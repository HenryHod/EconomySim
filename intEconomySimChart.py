from dash import Dash, dcc, html, Input, Output
import plotly.graph_objects as go
import sqlite3
import pandas as pd
from threading import Lock
import numpy as np
from scipy.interpolate import griddata
from scipy.ndimage import gaussian_filter

conn = sqlite3.connect(r"simulation.db", check_same_thread=False)
cursor = conn.cursor()
lock = Lock()
df = pd.read_sql("SELECT * FROM simulations", conn)

app = Dash(__name__)

app.layout = html.Div([
    dcc.Graph(id="figure"),
    html.Div([
    html.H4("Returns to Scale"),
    dcc.Slider(
        id='scale-slider',
        min = 0, max = 1, step = 0.1,
        value = 0.8),
    html.H4("Initial Endowment"),
    dcc.Slider(
        id='endowment-slider',
        min = 0, max = 400, step = 100,
        value = 200),
    html.H4("Charity"),
    dcc.Slider(
        id='charity-slider',
        min = 0, max = 1, step = 0.1,
        value = 0.5),
    html.H4("Skills"),
    dcc.Slider(
        id='skills-slider',
        min = 2, max=10, step=1,
        value=3)
    ],
    style={"width":"-webkit-fill-available"})
    ],
    style={"display":"flex"}
)

@app.callback(
    Output("figure", "figure"),
    Input("scale-slider", "value"),
    Input("endowment-slider", "value"),
    Input("charity-slider", "value"),
    Input("skills-slider", "value"))
def set_scale(scale_input, endow_input, char_input, skills_input):
    lock.acquire(True)
    scale_df = pd.DataFrame(conn.execute(f"""
    SELECT altruism, impatience, (CAST((new_children * 20) AS DOUBLE) / (good1 + good2)) AS child_per_dollar, (future_good1 + future_good2) AS total_goods FROM simulations
    WHERE good1_pref + good2_pref < {scale_input + 0.1} AND good1_pref + good2_pref > {scale_input - 0.1} 
    AND generation == 0
    AND good1 + good2 < {endow_input + 100} AND good1 + good2 > {endow_input - 100}
    AND charity < {char_input + 0.1} AND charity > {char_input - 0.1}
    AND skills == {skills_input}
    """).fetchall(), columns = ["altruism", "impatience", "child_per_dollar", "total_goods"])
    lock.release()
    print(scale_df.head())
    x = scale_df["altruism"]
    y = scale_df["impatience"]
    z = scale_df["child_per_dollar"]
    xi = np.linspace(min(x), max(x)+0.1)
    yi = np.linspace(min(y), max(y))
    X, Y = np.meshgrid(xi, yi)
    Z = gaussian_filter(griddata((x, y), z, (X.flatten(), Y.flatten()), 'nearest').reshape(50, 50), sigma=3)
    goods = gaussian_filter(griddata((x, y), scale_df['total_goods'], (X.flatten(), Y.flatten()), 'nearest').reshape(50, 50), sigma=3)
    layout = go.Layout(width=900,
                       height=900)
    fig = go.Figure(go.Surface(x = X, y = Y, z = Z, surfacecolor=goods, colorbar=dict(title = "Future Goods")), layout)
    fig.update_layout(scene = dict(xaxis_title="Altruism", yaxis_title="Impatience", zaxis_title="Children per Dollar", zaxis=dict(range=[0.0,1.0]), xaxis=dict(range=[0.5,1]), yaxis=dict(range=[0.5,1])))
    return fig

if __name__ == "__main__":
    app.run_server(debug=True)