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
#%%
data = []
df = pd.read_sql(f"""   SELECT 
                            pop_growth,
                            savings_rate,
                            cons_rate,
                            char_rate / 100 AS char_rate,
                            mean_altruism, 
                            mean_patience,
                            mean_charity, 
                            std,
                            period,
                            sim_id FROM groups
                        
                        """, conn)
#%%
df = df.groupby(["sim_id", "mean_altruism", "mean_patience", "mean_charity", "std"]).mean().drop("period", axis=1).reset_index()
#us_data = pd.read_csv("C:\\Users\\Henry\\Downloads\\avg_tax_rates.csv")[["Year", "Middle Quintile"]].rename({"Middle Quintile":"Tax Rate"}, axis = 1).dropna()
#%%
us_data = (fred.get_series("W055RC1", frequency="a")/fred.get_series("PI", frequency="a")).to_frame().rename({0:"Tax Rate"}, axis=1)
us_data['Cons Rate'] = (fred.get_series("PCE", frequency="a")/fred.get_series("PI", frequency="a")) * 100
us_data['Save Rate'] = fred.get_series("PSAVERT", frequency="a")
us_data['Pop Growth'] = fred.get_series("SPPOPGROWUSA", frequency="a")
us_data['Char Rate'] = (fred.get_series("IMZDCTCA", frequency="a")/(fred.get_series("PI", frequency="a") * 1000))
us_data.dropna(inplace=True)
print(us_data.head(10))
x = df["mean_altruism"]
y = df["mean_patience"]
z = df["mean_charity"]
xi = np.linspace(min(x), max(x), 100)
yi = np.linspace(min(y), max(y), 100)
X, Y = np.meshgrid(xi, yi)
Z = gaussian_filter(griddata((x, y), z, (X.flatten(), Y.flatten()), 'linear').reshape(100, 100), sigma = 1)
save = gaussian_filter(griddata((x, y), df["savings_rate"], (X.flatten(), Y.flatten()), 'linear').reshape(100, 100), sigma = 0)
cons = gaussian_filter(griddata((x, y), df["cons_rate"], (X.flatten(), Y.flatten()), 'linear').reshape(100, 100), sigma = 1)
char = gaussian_filter(griddata((x, y), df["char_rate"], (X.flatten(), Y.flatten()), 'linear').reshape(100, 100), sigma = 1)
pop = gaussian_filter(griddata((x, y), df["pop_growth"], (X.flatten(), Y.flatten()), 'linear').reshape(100, 100), sigma = 0)
#data.append(go.Surface(x = X, y = Y, z = pop, surfacecolor=save , colorscale="ice_r"))
countries = [[0.01, 0.046, 0.9]]
#%%
us_coord = {"x":[], "y":[], "z":[], "pop":[]}
errors = []
mu_p = np.mean(pop.flatten())
std_p = np.std(pop.flatten())
mu_s = np.mean(save.flatten())
std_s = np.std(save.flatten())
mu_c = np.mean(cons.flatten())
std_c = np.std(cons.flatten())
mu_ch = np.mean(char.flatten())
std_ch = np.std(char.flatten())
su_p = (pop - mu_p) / std_p
su_s = (save - mu_s) / std_s
su_c = (cons - mu_c) / std_c
su_ch = (char - mu_ch) / std_ch
for i, row in us_data.iterrows():
    errors.append(np.sqrt(
        (su_p - (row["Pop Growth"] - mu_p) / std_p) ** 2 +
        (su_s - (row["Save Rate"] - mu_s) / std_s) ** 2 +
        (su_c - (row["Cons Rate"] - mu_c) / std_c) ** 2 +
        (su_ch - (row["Char Rate"] - mu_ch) / std_ch) ** 2))
avg_error = np.mean(np.array(errors), axis=0)
min_index = np.where(avg_error == avg_error.min())
print(avg_error.min())
print(min_index[1][0], min_index[0][0])
print(X[min_index], Y[min_index], Z[min_index])
data.append(go.Scatter3d(x=us_data["Save Rate"], y=us_data["Cons Rate"], z=us_data["Char Rate"], marker=dict(color="red", size=2)))
#data.append(go.Scatter3d(x=us_coord["x"], y=us_coord["y"], z=us_coord["pop"], mode='markers'))
data.append(go.Scatter3d(x=df["savings_rate"], y=df["cons_rate"], z=df["char_rate"], mode="markers", marker=dict(color="blue", size=2), opacity=0.5))
data.append(go.Scatter3d(x=[save[min_index], save[min_index]], y=[cons[min_index], cons[min_index]], z=[char.min(), char.max()], mode='lines', line=dict(color='red')))
#print(us_coord["x"][min_index], us_coord["y"][min_index], us_coord["z"][min_index])
#%%
fig = go.Figure(data=data)
fig.update_layout(font=dict(size=14), scene = dict(xaxis_title="Savings Rate", yaxis_title="Consumption Rate", zaxis_title="Charity Rate"))
iplot(fig)
# %%
