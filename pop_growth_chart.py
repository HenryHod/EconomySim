from dash import Dash, dcc, html, Input, Output
import plotly.graph_objects as go
import sqlite3
import pandas as pd
from threading import Lock
import numpy as np
from scipy.interpolate import griddata
from scipy.ndimage import gaussian_filter
from plotly.subplots import make_subplots
from plotly.offline import iplot

conn = sqlite3.connect(r"simulation.db")
cursor = conn.cursor()
data = []
for a in range(4):
    df = pd.read_sql(f"""SELECT CAST(SUM(children) AS DOUBLE) / COUNT(id) AS pop_growth,
                            (CAST(future_goods AS DOUBLE) / goods) AS savings_rate,
                            mean_altruism, 
                            mean_patience, 
                            std FROM simulations
                        WHERE generation == 0 AND std == {a * 0.1}
                        GROUP BY mean_altruism, mean_patience, std
                        """, conn)
    x = df["mean_altruism"]
    y = df["mean_patience"]
    z = df["pop_growth"]
    xi = np.linspace(min(x), max(x))
    yi = np.linspace(min(y), max(y))
    X, Y = np.meshgrid(xi, yi)
    Z = griddata((x, y), z, (X.flatten(), Y.flatten()), 'linear').reshape(50, 50)
    goods = df['std']
    data.append(go.Surface(x = X, y = Y, z = Z, surfacecolor=goods))
fig = go.Figure(data=data)
iplot(fig)