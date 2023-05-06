#%%
from fredapi import Fred
import plotly.graph_objects as go
import matplotlib.pyplot as plt
import sqlite3
import pandas as pd
from threading import Lock
import numpy as np
from scipy.interpolate import griddata
from scipy.ndimage import gaussian_filter
from plotly.subplots import make_subplots
from plotly.offline import iplot
fred = Fred(api_key="179a8a574defbe5d2bb69cc07b59beb2")
max_age = 3
conn = sqlite3.connect(r"simulation.db")
cursor = conn.cursor()
childcost = 20
#%%
data = []
full_df = pd.read_sql(f"""   SELECT 
                            (CAST((population - start_population) AS DOUBLE) / start_population) * 100 AS pop_growth,
                            (CAST(future_goods AS DOUBLE) / goods) * 100 AS savings_rate,
                            (CAST((self_goods) AS DOUBLE) / goods) * 100 AS cons_rate,
                            (CAST(char_goods As DOUBLE) / goods) * 100 AS char_rate,
                            mean_altruism, 
                            mean_patience,
                            mean_charity,
                            period,
                            max_start,
                            sim_id FROM economies
                        
                        """, conn)
#%%
full_df = full_df.groupby(["mean_altruism", "mean_patience", "mean_charity", "sim_id", "max_start"]).mean().reset_index()
print(full_df)
#df["pop_growth"] = df.groupby(["mean_altruism", "mean_patience", "mean_charity", "sim_id"])["population"].pct_change()
full_df["total"] = full_df["savings_rate"] + full_df["cons_rate"] + full_df["char_rate"]
#df = df.query("period > 5")
#us_data = pd.read_csv("C:\\Users\\Henry\\Downloads\\avg_tax_rates.csv")[["Year", "Middle Quintile"]].rename({"Middle Quintile":"Tax Rate"}, axis = 1).dropna()
#%%
us_data = (fred.get_series("W055RC1", frequency="a")/fred.get_series("PI", frequency="a")).to_frame().rename({0:"Tax Rate"}, axis=1)
us_data['Cons Rate'] = (fred.get_series("PCE", frequency="a")/fred.get_series("PI", frequency="a")) * 100
us_data['Save Rate'] = fred.get_series("PSAVERT", frequency="a")
us_data['Pop Growth'] = fred.get_series("SPPOPGROWUSA", frequency="a")
us_data['Char Rate'] = (fred.get_series("IMZDCTCA", frequency="a")/(fred.get_series("PI", frequency="a") * 1000))
us_data.dropna(inplace=True)
centroid = (np.mean(us_data['Pop Growth']), np.mean(us_data['Save Rate']), np.mean(us_data['Cons Rate']), np.mean(us_data['Char Rate']))
#print(us_data.head(10))
ca_data = pd.read_csv("ca_data.csv")
#print(ca_data)
#%%
cent_errors = []
cp_errors = []
cp_parameters = []
smooth_errors = []
smooth_parameters = []
start_groups = np.unique(full_df["max_start"])
for start_group in start_groups:
    df = full_df.query(f"max_start == {start_group}").reset_index()
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
    us_coord = {"x":[], "y":[], "z":[], "pop":[]}
    errors = []
    errors_est = []
    pop_std = np.std(pop.flatten())
    x_std = np.std(X.flatten())
    y_std = np.std(Y.flatten())
    z_std = np.std(Z.flatten())
    p_std = np.std(df["pop_growth"])
    save_std = np.std(df["savings_rate"])
    cons_std = np.std(df["cons_rate"])
    char_std = np.std(df["char_rate"])
    mean_errors = np.sqrt(
            ((df["pop_growth"] - centroid[0]) / p_std) ** 2 + 
            ((df["savings_rate"] - centroid[1]) / save_std) ** 2 + 
            ((df["cons_rate"] - centroid[2]) / cons_std) ** 2 + 
            ((df["char_rate"] - centroid[3]) / char_std) ** 2
        )
    for i, row in us_data.reset_index().iterrows():
        errors_est.append(np.sqrt(
            ((df["pop_growth"] - row["Pop Growth"]) / p_std) ** 2 + 
            ((df["savings_rate"] - row["Save Rate"]) / save_std) ** 2 + 
            ((df["cons_rate"] - row["Cons Rate"]) / cons_std) ** 2 + 
            ((df["char_rate"] - row["Char Rate"]) / char_std) ** 2
        ))
        errors.append(np.sqrt(
            ((pop.flatten() - row["Pop Growth"]) / pop_std) ** 2 +
            ((X.flatten() - row["Save Rate"]) / x_std) ** 2 +
            ((Y.flatten() - row["Cons Rate"]) / y_std) ** 2 +
            ((Z.flatten() - row["Char Rate"]) / z_std) ** 2))

    avg_error_est = np.mean(np.array(errors_est), axis=0)
    min_indexes_est = np.argpartition(avg_error_est, 20)[:20]
    mean_min_indexes = np.argpartition(mean_errors, 20)[:20]
    cent_errors.append(np.mean(mean_errors[mean_min_indexes]))
    cp_errors.append(np.mean(avg_error_est[min_indexes_est]))
    cp_parameters.append([np.mean(df["mean_altruism"][min_indexes_est]), np.mean(df["mean_patience"][min_indexes_est]), np.mean(df["mean_charity"][min_indexes_est])])
    avg_error = np.mean(np.array(errors), axis=0)
    min_indexes = np.argpartition(avg_error, 20)[:20]
    min_index = np.where(avg_error == avg_error.min())
    smooth_errors.append(np.mean(avg_error[min_indexes]))
    #print(np.std(alt.flatten()[min_indexes]), np.std(pat.flatten()[min_indexes])) 
    smooth_parameters.append([np.mean(alt.flatten()[min_indexes]), np.mean(pat.flatten()[min_indexes]), np.mean(char.flatten()[min_indexes])])
    #data.append(go.Scatter3d(x=X.flatten()[min_indexes], y=Y.flatten()[min_indexes], z=Z.flatten()[min_indexes]))
    #data.append(go.Surface(x=X, y=Y, z=Z, surfacecolor=pop))
    #data.append(go.Scatter3d(x=[save[min_index], save[min_index]], y=[cons[min_index], cons[min_index]], z=[char.min(), char.max()], mode='lines', line=dict(color='red')))
    #print(us_coord["x"][min_index], us_coord["y"][min_index], us_coord["z"][min_index])
#%%
data.append(go.Scatter3d(x=us_data["Save Rate"], y=us_data["Cons Rate"], z=us_data["Char Rate"], marker=dict(color="red", size=5)))
data.append(go.Scatter3d(x=ca_data["Save Rate"], y=ca_data["Cons Rate"], z=ca_data["Char Rate"], marker=dict(color="orange", size=5)))
#data.append(go.Scatter3d(x=us_coord["x"], y=us_coord["y"], z=us_coord["pop"], mode='markers'))
data.append(go.Scatter3d(x=df["savings_rate"], y=df["cons_rate"], z=df["char_rate"], mode='markers',marker=dict(color=df["pop_growth"], size=3, colorscale="Reds"), opacity=0.5,
                        hovertemplate = "Savings Rate: %{x}<br>" +
                        "Consumption Rate: %{y}<br>" +
                        "Charity Rate %{z}<br>" +
                        "Population Growth: %{marker.color:,}" +
                        "<extra></extra>",
                        ))
data.append(go.Scatter3d(x=X.flatten()[min_indexes], y=Y.flatten()[min_indexes], z=Z.flatten()[min_indexes], mode='markers',marker=dict(color=pop.flatten()[min_indexes], size=4), opacity=0.5,
hovertemplate = "Savings Rate: %{x}<br>" +
                        "Consumption Rate: %{y}<br>" +
                        "Charity Rate %{z}<br>" +
                        "Population Growth: %{marker.color:,}" +
                        "<extra></extra>"))
#data.append(go.Scatter3d(x=df["savings_rate"][mean_min_indexes], y=df["cons_rate"][mean_min_indexes], z=df["char_rate"][mean_min_indexes], mode='markers',marker=dict(color="purple", size=4), opacity=0.5))
data.append(go.Scatter3d(x=[centroid[1]], y=[centroid[2]], z=[centroid[3]], mode='markers',marker=dict(color="green", size=6), opacity=0.5))
#fig = go.Figure(data=data)
#fig.update_layout(font=dict(size=14), scene = dict(xaxis_title="Savings Rate", yaxis_title="Consumption Rate", zaxis_title="Charity Rate"))
#iplot(fig)
# %%
fig, axs = plt.subplots(1, 2)
axs[0].plot(start_groups, smooth_errors)
axs[0].set_title("Error")
axs[1].plot(start_groups, smooth_parameters, label=["Altruism", "Patience", "Charity"])
axs[1].set_title("Parameters")
plt.legend()
plt.show()
#max_df = df.groupby(["mean_altruism", "mean_patience", "mean_charity"]).mean().reset_index()
#fig2 = go.Figure(data=[go.Scatter3d(x=max_df["mean_altruism"], y=max_df["mean_patience"], z=max_df["pop_growth"], mode="markers", marker=dict(color=max_df["total"]))])
# %%
