#%%
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
childcost = 20
#%%
data = []
df = pd.read_sql(f"""   SELECT 
                            (CAST((children - prev_children) AS DOUBLE) / population) * 100 AS pop_growth,
                            (CAST(future_goods AS DOUBLE) / goods) * 100 AS savings_rate,
                            (CAST((self_goods) AS DOUBLE) / goods) * 100 AS cons_rate,
                            (CAST(char_goods As DOUBLE) / goods) * 100 AS char_rate,
                            mean_altruism, 
                            mean_patience,
                            mean_charity,
                            period,
                            sim_id FROM economies
                        
                        """, conn)
#%%
#df = df.groupby(["mean_altruism", "mean_patience", "mean_charity", "sim_id"]).mean().reset_index()
print(df)
df["total"] = df["savings_rate"] + df["cons_rate"] + df["char_rate"]
#df = df.query("period > 3")
#us_data = pd.read_csv("C:\\Users\\Henry\\Downloads\\avg_tax_rates.csv")[["Year", "Middle Quintile"]].rename({"Middle Quintile":"Tax Rate"}, axis = 1).dropna()
#%%
us_data = (fred.get_series("W055RC1", frequency="a")/fred.get_series("PI", frequency="a")).to_frame().rename({0:"Tax Rate"}, axis=1)
us_data['Cons Rate'] = (fred.get_series("PCE", frequency="a")/fred.get_series("PI", frequency="a")) * 100
us_data['Save Rate'] = fred.get_series("PSAVERT", frequency="a")
us_data['Pop Growth'] = fred.get_series("SPPOPGROWUSA", frequency="a")
us_data['Char Rate'] = (fred.get_series("IMZDCTCA", frequency="a")/(fred.get_series("PI", frequency="a") * 1000))
us_data.dropna(inplace=True)
print(us_data.head(10))
ca_data = pd.read_csv("ca_data.csv")
print(ca_data)
x = df["savings_rate"]
y = df["cons_rate"]
z = df["char_rate"]
xi = np.linspace(min(x), max(x), 1000)
yi = np.linspace(min(y), max(y), 1000)
X, Y = np.meshgrid(xi, yi)
Z = np.nan_to_num(gaussian_filter(griddata((x, y), z, (X.flatten(), Y.flatten()), 'linear').reshape(1000, 1000), sigma = 0), nan = 0)
alt = np.nan_to_num(gaussian_filter(griddata((x, y), df["mean_altruism"], (X.flatten(), Y.flatten()), 'nearest').reshape(1000, 1000), sigma = 1), nan = 0)
pat = np.nan_to_num(gaussian_filter(griddata((x, y), df["mean_patience"], (X.flatten(), Y.flatten()), 'nearest').reshape(1000, 1000), sigma = 1), nan = 0)
char = np.nan_to_num(gaussian_filter(griddata((x, y), df["mean_charity"], (X.flatten(), Y.flatten()), 'nearest').reshape(1000, 1000), sigma = 1), nan = 0)
pop = np.nan_to_num(gaussian_filter(griddata((x, y), df["pop_growth"], (X.flatten(), Y.flatten()), 'linear').reshape(1000, 1000), sigma = 0), nan = 0)
#data.append(go.Surface(x = X, y = Y, z = pop, surfacecolor=save , colorscale="ice_r"))
countries = [[0.01, 0.046, 0.9]]
#%%
us_coord = {"x":[], "y":[], "z":[], "pop":[]}
errors = []
pop_mean = np.mean(pop.flatten())
pop_std = np.std(pop.flatten())
x_mean = np.mean(X.flatten())
x_std = np.std(X.flatten())
y_mean = np.mean(Y.flatten())
y_std = np.std(Y.flatten())
z_mean = np.mean(Z.flatten())
z_std = np.std(Z.flatten())


for i, row in us_data.reset_index().iterrows():
    errors.append(np.sqrt(
        ((pop.flatten() - row["Pop Growth"]) / pop_std) ** 2 +
        ((X.flatten() - row["Save Rate"]) / x_std) ** 2 +
        ((Y.flatten() - row["Cons Rate"]) / y_std) ** 2 +
        ((Z.flatten() - row["Char Rate"]) / z_std) ** 2))
avg_error = np.mean(np.array(errors), axis=0)
min_indexes = np.argpartition(avg_error, 50)[:50]
min_index = np.where(avg_error == avg_error.min())
print(np.mean(avg_error[min_indexes]))
print(np.std(alt.flatten()[min_indexes]), np.std(pat.flatten()[min_indexes])) 
print(np.mean(alt.flatten()[min_indexes]), np.mean(pat.flatten()[min_indexes]), np.mean(char.flatten()[min_indexes]))
data.append(go.Scatter3d(x=us_data["Save Rate"], y=us_data["Cons Rate"], z=us_data["Char Rate"], marker=dict(color="red", size=4)))
data.append(go.Scatter3d(x=ca_data["Save Rate"], y=ca_data["Cons Rate"], z=ca_data["Char Rate"], marker=dict(color="orange", size=4)))
#data.append(go.Scatter3d(x=us_coord["x"], y=us_coord["y"], z=us_coord["pop"], mode='markers'))
data.append(go.Scatter3d(x=df["savings_rate"], y=df["cons_rate"], z=df["char_rate"], mode='markers',marker=dict(color="blue", size=4), opacity=0.5))
data.append(go.Scatter3d(x=X.flatten()[min_indexes], y=Y.flatten()[min_indexes], z=Z.flatten()[min_indexes]))
#data.append(go.Surface(x=X, y=Y, z=Z, surfacecolor=pop))
#data.append(go.Scatter3d(x=[save[min_index], save[min_index]], y=[cons[min_index], cons[min_index]], z=[char.min(), char.max()], mode='lines', line=dict(color='red')))
#print(us_coord["x"][min_index], us_coord["y"][min_index], us_coord["z"][min_index])
#%%
fig = go.Figure(data=data)
fig.update_layout(font=dict(size=14), scene = dict(xaxis_title="Savings Rate", yaxis_title="Consumption Rate", zaxis_title="Charity Rate"))
iplot(fig)
# %%
