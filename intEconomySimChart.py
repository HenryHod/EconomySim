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

conn = sqlite3.connect(r"simulation.db", check_same_thread=False)
cursor = conn.cursor()
lock = Lock()
df = pd.read_sql("SELECT * FROM simulations", conn)
childcost = 20

app = Dash(__name__)

app.layout = html.Div([
    dcc.Graph(id="figure"),
    html.Div([
    html.H4("Returns to Scale"),
    dcc.Slider(
        id='scale-slider',
        min = 0, max = 1, step = 0.1,
        value = 0.8),
    html.H4("Initial Endowment 1"),
    dcc.Slider(
        id='endowment-slider1',
        min = childcost, max = childcost * 10, step = childcost,
        value = childcost * 2),
    html.H4("Initial Endowment 2"),
    dcc.Slider(
        id='endowment-slider2',
        min = childcost, max = childcost * 10, step = childcost,
        value = childcost * 8),
    html.H4("Charity"),
    dcc.Slider(
        id='charity-slider',
        min = 0, max = 1, step = 0.1,
        value = 0)
    ],
    style={"width":"-webkit-fill-available"})
    ],
    style={"display":"flex"}
)

@app.callback(
    Output("figure", "figure"),
    Input("scale-slider", "value"),
    Input("endowment-slider1", "value"),
    Input("endowment-slider1", "value"),
    Input("charity-slider", "value"))
def set_scale(scale_input, endow_input1, endow_input2, char_input):
    lock.acquire(True)
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
    lock.release()
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
    data = [
        go.Surface(x = X, y = Y, z = Z, surfacecolor=goods, colorscale="blues", showscale=False, colorbar=dict(title = "Savings Rate")),
        go.Surface(x = X, y = Y, z = Z12, surfacecolor=goods2, colorscale="greens", showscale=False, colorbar=dict(title = "Savings Rate"))
        ]
    fig = go.Figure(data=data)
    #fig = make_subplots(rows=1, cols=2, specs=[[{'type': 'surface'}, {'type': 'surface'}]])
    #fig.add_trace(go.Surface(x = X, y = Y, z = Z, surfacecolor=goods, colorbar=dict(title = "Savings Rate"), colorscale="blues"), row=1, col=1)
    #fig.add_trace(go.Surface(x = X, y = Y, z = Z12, surfacecolor=goods2, colorbar=dict(title = "Savings Rate"), colorscale="greens"), row=1, col=1)
    #fig.add_trace(go.Surface(x = X, y = Y, z = Z2, surfacecolor=goods, colorbar=dict(title = "Savings Rate"), colorscale="blues"), row=1, col=2)
    #fig.add_trace(go.Surface(x = X, y = Y, z = Z22, surfacecolor=goods2, colorbar=dict(title = "Savings Rate"), colorscale="greens"), row=1, col=2)
    #fig.update_xaxes()
    #fig.update_layout(scene = dict(xaxis_title="Altruism", yaxis_title="patience", zaxis_title="Children per Dollar",
    #zaxis=dict(range=[0.0,1.0]), xaxis=dict(range=[0.0,1]), yaxis=dict(range=[0.0,1])),
    #scene2 = dict(xaxis_title="Altruism", yaxis_title="patience", zaxis_title="MPC"), height=800,width=1200)
    return fig

if __name__ == "__main__":
    app.run_server(debug=True)