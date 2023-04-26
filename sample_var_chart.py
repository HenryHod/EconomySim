#%%
import sqlite3
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
conn = sqlite3.connect(r"simulation.db")
cursor = conn.cursor()
childcost = 20
cursor.execute("""
        DROP TABLE IF EXISTS sample_pops
        """)
cursor.execute(f"""
        CREATE TABLE sample_pops AS
            SELECT 
            (CAST(SUM(children - prev_children) AS DOUBLE) / COUNT(id)) * 100 AS pop_growth,
            (CAST(future_goods AS DOUBLE) / goods) * 100 AS savings_rate,
            ((CAST(self_goods AS DOUBLE) + (children - prev_children) * {childcost}) / goods) * 100 AS cons_rate,
            (CAST(char_goods AS DOUBLE) / goods) AS char_rate,
            COUNT(CASE WHEN (period == 1) THEN id END) AS sample_size,
            sim_id,
            period
            FROM simulations
            WHERE age > 0
            GROUP BY sim_id, period
        """)
#%%
df = pd.read_sql(f"""
            SELECT * FROM sample_pops
            """, conn)
samples = df.query("sample_size != 0").set_index("sim_id")["sample_size"].to_dict()
df["sample_size"] = df["sim_id"].apply(lambda x: samples[x])
vars = df.groupby("sim_id").var().groupby("sample_size").var().drop("period", axis=1).reset_index()
print(vars)
fig, axs = plt.subplots(2, 2)
axs[0, 0].plot(vars["sample_size"], vars["pop_growth"])
axs[0, 1].plot(vars["sample_size"], vars["savings_rate"])
axs[1, 0].plot(vars["sample_size"], vars["cons_rate"])
axs[1, 1].plot(vars["sample_size"], vars["char_rate"])
axs[0, 0].setx_scale('log')
plt.show()
# %%
