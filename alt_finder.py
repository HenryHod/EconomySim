# %%
from fredapi import Fred
import plotly.graph_objects as go
import matplotlib.pyplot as plt
import sqlite3
import pandas as pd
from threading import Lock
import numpy as np
fred = Fred(api_key="179a8a574defbe5d2bb69cc07b59beb2")
max_age = 3
conn = sqlite3.connect(r"simulation.db")
cursor = conn.cursor()
#%%
df = pd.read_sql(f"""   SELECT 
                            (CAST((population - start_population) AS DOUBLE) / start_population) * 100 AS pop_growth,
                            (CAST(future_goods AS DOUBLE) / goods) * 100 AS savings_rate,
                            (CAST((self_goods) AS DOUBLE) / goods) * 100 AS cons_rate,
                            (CAST(char_goods As DOUBLE) / goods) * 100 AS char_rate,
                            mean_altruism, 
                            mean_patience,
                            mean_charity,
                            period,
                            sim_id FROM economies
                        
                        """, conn)
plt.scatter(df["mean_altruism"], df["pop_growth"], c=df["period"])
plt.colorbar()
plt.show()