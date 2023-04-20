from dash import Dash, dcc, html, Input, Output
from fredapi import Fred
import plotly.graph_objects as go
import sqlite3
import pandas as pd
from threading import Lock
import numpy as np
from scipy.interpolate import griddata
from scipy.ndimage import gaussian_filter
from plotly.subplots import make_subplots
from plotly.offline import iplot
from math import isclose
fred = Fred(api_key="179a8a574defbe5d2bb69cc07b59beb2")
max_age = 3
conn = sqlite3.connect(r"simulation.db")
cursor = conn.cursor()
data = []
df = pd.read_sql(f"""SELECT (CAST(SUM(children - prev_children) AS DOUBLE) / COUNT(id)) * 100 AS pop_growth,
                            (CAST(future_goods AS DOUBLE) / goods) * 100 AS savings_rate,
                            (CAST((self_goods) AS DOUBLE) / (goods)) * 100 AS cons_rate,
                            mean_altruism, 
                            mean_patience, 
                            std,
                            period FROM simulations
                        WHERE std == {0.05} AND age > {0}
                        GROUP BY period, mean_altruism, mean_patience, std
                        """, conn)
df = df.groupby(["mean_altruism", "mean_patience", "std"]).mean().drop("period", axis=1).reset_index()
us_data = pd.read_csv("C:\\Users\\Henry\\Downloads\\avg_tax_rates.csv")[["Year", "Middle Quintile"]].rename({"Middle Quintile":"Tax Rate"}, axis = 1).dropna()
us_data['Year'] = pd.to_datetime(us_data["Year"], format="%Y")
us_data.set_index("Year", inplace=True)
us_data['Cons Rate'] = (fred.get_series("PCE", frequency="a")/fred.get_series("PI", frequency="a")) * 100 + us_data['Tax Rate']
us_data['Save Rate'] = fred.get_series("PSAVERT", frequency="a")
us_data['Pop Growth'] = fred.get_series("SPPOPGROWUSA", frequency="a")
print(us_data.head(10))
x = df["mean_altruism"]
y = df["mean_patience"]
z = df["pop_growth"]
xi = np.linspace(min(x), max(x))
yi = np.linspace(min(y), max(y))
X, Y = np.meshgrid(xi, yi)
Z = gaussian_filter(griddata((x, y), z, (X.flatten(), Y.flatten()), 'linear').reshape(50, 50), sigma = 10)
save = gaussian_filter(griddata((x, y), df["savings_rate"], (X.flatten(), Y.flatten()), 'linear').reshape(50, 50), sigma = 5)
cons = gaussian_filter(griddata((x, y), df["cons_rate"], (X.flatten(), Y.flatten()), 'linear').reshape(50, 50), sigma = 5)
data.append(go.Surface(x = X, y = Y, z = Z, surfacecolor=save , colorscale="ice_r"))
countries = [[0.01, 0.046, 0.9]]
us_coord = {"x":[], "y":[], "z":[]}
errors = {"x":[], "y":[], "z":[]}
mu_z = np.mean(Z.flatten())
std_z = np.std(Z.flatten())
mu_s = np.mean(save.flatten())
std_s = np.std(save.flatten())
mu_c = np.mean(cons.flatten())
std_c = np.std(cons.flatten())
su_z = (Z - mu_z)/std_z
su_s = (save - mu_s)/std_s
su_c = (cons - mu_c)/std_c
for i, row in us_data.iterrows():
    error = np.sqrt((su_z - (row["Pop Growth"] - mu_z) / std_z) ** 2 + (su_s - (row["Save Rate"] - mu_s) / std_s) ** 2 + (su_c - (row["Cons Rate"] - mu_c) / std_c) ** 2)

    min_finder = error == error.min()
    errors["x"].append(Z[min_finder][0] - row["Pop Growth"])
    errors["y"].append(save[min_finder][0] - row["Save Rate"])
    errors["z"].append(cons[min_finder][0] - row["Cons Rate"])
    us_coord["x"].append(X[min_finder][0])
    us_coord["y"].append(Y[min_finder][0])
    us_coord["z"].append(Z[min_finder][0])
print(errors)
data.append(go.Scatter3d(x=us_coord["x"], y=us_coord["y"], z=us_coord["z"], mode='markers'))
data.append(go.Scatter3d(x=[np.median(us_coord["x"]), np.median(us_coord["x"])], y=[np.median(us_coord["y"]), np.median(us_coord["y"])], z=[min(z), max(z)], mode='lines', line=dict(color='red')))
fig = go.Figure(data=data)
fig.update_layout(font=dict(size=14), scene = dict(xaxis_title="Mean Altruism", yaxis_title="Mean Patience", zaxis_title="Population Growth"))
iplot(fig)