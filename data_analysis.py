import sqlite3
import pandas as pd
import numpy as np
import statsmodels.api as sm
from mpl_toolkits import mplot3d
import matplotlib.pyplot as plt
import plotly.graph_objects as go
from scipy.interpolate import griddata
conn = sqlite3.connect(r"simulation.db")
cursor = conn.cursor()
df = pd.read_sql("SELECT * FROM simulations", conn)
def jitter(values,j):
    return values + np.random.normal(j,0.1,values.shape)
"""
family_size_dict = df.groupby(["family", "period"])["id"].count().to_dict()
df["family size"] = df.apply(lambda x: family_size_dict[(x['family'], x['period'])], axis = 1)
df["total goods"] = df["good1"] + df["good2"]
df["future total goods"] = df["future_good1"] + df["future_good2"]
family_wealth_dict = df.groupby(["family", "period"])["total goods"].sum().to_dict()
df["family wealth"] = df.apply(lambda x: family_wealth_dict[(x['family'], x['period'])], axis = 1)
df["returns to scale"] = df["good1_pref"] + df["good2_pref"]
df["log children"] = np.log(df["children"] + 1)
df["log total goods"] = np.log(df["total goods"]).fillna(0)
df["log family size"] = np.log(df["family size"]).fillna(0)
df["log utility"] = np.log(df["utility"]).fillna(0)
#df["altruism * returns to scale"] = df["altruism"] * df["returns to scale"]
#df["altruism * log total goods"] = df["altruism"] * df["log total goods"]
#df["altruism * impatience"] = df["altruism"] * df["impatience"]
#df = pd.get_dummies(df,columns=["age", "generation"])
bad_columns = ["children", "log children", "good1","good2", "future_good1", "future_good2", "good1_pref", "good2_pref", "period", "family", "impatience", "charity", "returns to scale", "utility", "log utility", "skills", "id", "new_children"]
for column1 in df.columns[:21]:
    for column2 in df.columns[:21]:
        if (column1 not in bad_columns and column2 not in bad_columns) and (f"{column2} * {column1}" not in df.columns):
            #print(column1, column2)
            df[f"{column1} * {column2}"] = df[column1] * df[column2]
df.replace([np.inf, -np.inf], np.nan, inplace=True)
df.dropna(inplace=True)
#print(df)
#g = sns.FacetGrid(df.query("generation == 0"), col="age")
#plt.plot(np.unique(df["period"]), df.groupby("period")["id"].count().pct_change())
#model = sm.OLS(df["new_children"], df.drop(bad_columns, axis = 1))
#result = model.fit(cov_type="HC0")
#print(result.summary())
"""
age0_df = df.query("period == 1").query("generation == 0")
x = age0_df["altruism"]
y = age0_df["impatience"]
z = age0_df["new_children"]
xi = np.linspace(min(x), max(x))
yi = np.linspace(min(y), max(y))
X, Y = np.meshgrid(xi, yi)
Z = griddata((x, y), z, (X.flatten(), Y.flatten()), 'linear').reshape(50, 50)
print(type(Z))
fig = go.Figure(go.Surface(x = X, y = Y, z = Z))
#plt.scatter(age0_df["altruism"], jitter(age0_df["new_children"], 0), c=age0_df["impatience"])
#g.map(sns.scatterplot, "altruism","log children")
#ax.colorbar()
#plt.xlim(0.7, 1)
#plt.colorbar()
#ax.set_xlabel('altruism')
#ax.set_ylabel('impatience')
#ax.set_zlabel('new_children')
#ax.set_ylim(0.5, 1)
#plt.show()
fig.show()
