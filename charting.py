from dash import Dash, dcc, html, Input, Output
import plotly.graph_objects as go
import sqlite3
import pandas as pd
from threading import Lock
import numpy as np
from scipy.interpolate import griddata
from scipy.ndimage import gaussian_filter
from plotly.subplots import make_subplots
from plotly.offline import iplot, init_notebook_mode
from plotly import tools as tls
conn = sqlite3.connect(r"simulation.db", check_same_thread=False)
cursor = conn.cursor()
#lock = Lock()
df = pd.read_sql("SELECT * FROM simulations", conn)
childcost = 20
scale_input=0.8
endow_input1 = 8 * childcost
endow_input2 = 2 * childcost
char_input = 0
scale_df = pd.DataFrame(conn.execute(f"""
    SELECT altruism, patience,
        (CAST(((children - prev_children) * {childcost}) AS DOUBLE) / goods) AS child_per_dollar, 
        (CAST(future_goods AS DOUBLE) / goods) AS savings_rate, 
        (CAST((self_goods) AS DOUBLE) / (goods)) AS mpc FROM simulations
    WHERE pref < {scale_input + 0.1} AND pref > {scale_input - 0.1} 
    AND generation == 0
    AND goods < {endow_input1 + childcost} AND goods > {endow_input1 - childcost}
    AND charity < {char_input + 0.1} AND charity > {char_input - 0.1}
    """).fetchall(), columns = ["altruism", "patience", "child_per_dollar", "savings_rate", "mpc"])
scale_df2 = pd.DataFrame(conn.execute(f"""
SELECT altruism, patience,
    (CAST(((children - prev_children) * {childcost}) AS DOUBLE) / goods) AS child_per_dollar, 
    (CAST(future_goods AS DOUBLE) / goods) AS savings_rate, 
    (CAST((self_goods) AS DOUBLE) / (goods)) AS mpc FROM simulations
WHERE pref < {scale_input + 0.1} AND pref > {scale_input - 0.1} 
AND generation == 0
AND goods < {endow_input2 + childcost} AND goods > {endow_input2 - childcost}
AND charity < {char_input + 0.1} AND charity > {char_input - 0.1}
""").fetchall(), columns = ["altruism", "patience", "child_per_dollar", "savings_rate", "mpc"])
#lock.release()
print(scale_df.head())
x = scale_df["altruism"]
x2 = scale_df2["altruism"]
y = scale_df["patience"]
y2 = scale_df2["patience"]
z = scale_df["child_per_dollar"]
z12 = scale_df2["child_per_dollar"]
z2 = scale_df["mpc"]
z22 = scale_df2["mpc"]
xi = np.linspace(min(x), max(x) + 0.1)
yi = np.linspace(min(y), max(y))
X, Y = np.meshgrid(xi, yi)
Z = gaussian_filter(griddata((x, y), z, (X.flatten(), Y.flatten()), 'nearest').reshape(50, 50), sigma=3)
Z12 = gaussian_filter(griddata((x2, y2), z12, (X.flatten(), Y.flatten()), 'nearest').reshape(50, 50), sigma=3)
Z2 = gaussian_filter(griddata((x, y), z2, (X.flatten(), Y.flatten()), 'nearest').reshape(50, 50), sigma=3)
Z22 = gaussian_filter(griddata((x2, y2), z22, (X.flatten(), Y.flatten()), 'nearest').reshape(50, 50), sigma=3)
goods = gaussian_filter(griddata((x, y), scale_df['savings_rate'], (X.flatten(), Y.flatten()), 'nearest').reshape(50, 50), sigma=3)
goods2 = gaussian_filter(griddata((x2, y2), scale_df2['savings_rate'], (X.flatten(), Y.flatten()), 'nearest').reshape(50, 50), sigma=3)
figz = make_subplots(cols=2, rows=1, specs=[[{'type': 'surface'}, {'type': 'surface'}]])
figz.append_trace(go.Surface(x = X, y = Y, z = Z, surfacecolor=goods, colorscale="solar_r", showscale=False, colorbar=dict(title = "Savings Rate")),row=1, col=1)
figz.append_trace(go.Surface(x = X, y = Y, z = Z12, surfacecolor=goods2, colorscale="ice_r", showscale=False, colorbar=dict(title = "Savings Rate")),row=1, col=1)
figz.append_trace(go.Surface(x = X, y = Y, z = Z2, surfacecolor=goods, showscale=False, colorbar=dict(title = "Savings Rate"), colorscale="solar_r"), row=1, col=2)
figz.append_trace(go.Surface(x = X, y = Y, z = Z22, surfacecolor=goods2, showscale=False, colorbar=dict(title = "Savings Rate"), colorscale="ice_r"), row=1, col=2)
eyes = [dict(x=1, y=2, z=0.5),
        dict(x=-1.5, y=2, z=0.4)
]
figz.layout.scene.camera.eye = eyes[0]
figz.layout.scene2.camera.eye = eyes[1]
figz.update_layout(scene = dict(xaxis_title="Altruism", yaxis_title="patience", zaxis_title="Children per Dollar",
    zaxis=dict(range=[0.0,1.0]), xaxis=dict(range=[0.0,1]), yaxis=dict(range=[0.0,1])), 
    scene2 = dict(xaxis_title="Altruism", yaxis_title="patience", zaxis_title="Consumption per Dollar",
    zaxis=dict(range=[0.0,1.0]), xaxis=dict(range=[0.0,1]), yaxis=dict(range=[0.0,1])))
iplot(figz)