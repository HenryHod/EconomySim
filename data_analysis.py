import sqlite3
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
import statsmodels.api as sm
import copy
conn = sqlite3.connect(r"simulation.db")
cursor = conn.cursor()
df = pd.read_sql("SELECT * FROM simulations", conn)
def jitter(values,j):
    return values + np.random.normal(j,0.1,values.shape)
family_size_dict = df.groupby(["family", "period"])["id"].count().to_dict()
df["family size"] = df.apply(lambda x: family_size_dict[(x['family'], x['period'])], axis = 1)
df["total goods"] = df["good1"] + df["good2"]
family_wealth_dict = df.groupby(["family", "period"])["total goods"].sum().to_dict()
df["family wealth"] = df.apply(lambda x: family_wealth_dict[(x['family'], x['period'])], axis = 1)
df["returns to scale"] = df["good1_pref"] + df["good2_pref"]
df["log children"] = np.log(df["children"]).fillna(0)
df["log total goods"] = np.log(df["total goods"]).fillna(0)
df["log family size"] = np.log(df["family size"])
df["log utility"] = np.log(df["utility"]).fillna(0)
#df["altruism * returns to scale"] = df["altruism"] * df["returns to scale"]
#df["altruism * log total goods"] = df["altruism"] * df["log total goods"]
#df["altruism * impatience"] = df["altruism"] * df["impatience"]
#df = pd.get_dummies(df,columns=["age", "generation"])
print(df.query("family == 984").query("generation == 1"))
bad_columns = ["children", "log children", "good1","good2", "good1_pref", "good2_pref", "period", "family", "impatience", "charity", "returns to scale", "utility", "log utility", "skills", "id"]
for column1 in df.columns[:21]:
    for column2 in df.columns[:21]:
        if (column1 not in bad_columns and column2 not in bad_columns) and (f"{column2} * {column1}" not in df.columns):
            print(column1, column2)
            df[f"{column1} * {column2}"] = df[column1] * df[column2]
df = df[df["log total goods"] >= 0]
df = df[df["log children"] >= 0]
g = sns.FacetGrid(df, col="generation * generation")
model = sm.OLS(df["log children"], df.drop(bad_columns, axis = 1))
result = model.fit(cov_type="HC0")
#print(result.summary())
#plt.scatter(df["altruism"], jitter(df["log children"], 0.5), c=df["generation"])
#g.map(sns.scatterplot, "altruism","log children")
#plt.show()