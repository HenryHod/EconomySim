import pandas as pd
import json
import time
import sqlite3
start = time.time()
periods_dict = json.load(open("EconomySimData.json"))["periods"]
df = pd.DataFrame(columns=["id", "Period", "Family", "Family Size", "children", "altruism", "charity", "patience", "skills", "good1 pref", "good2 pref", "good1", "good2", "future good1", "future good2"])
for period in periods_dict.keys():
    for family in periods_dict[period]["families"].keys():
        if "individuals" in periods_dict[period]["families"][family].keys():
            for individual in periods_dict[period]["families"][family]["individuals"].keys():
                ind_dict = periods_dict[period]["families"][family]["individuals"][individual]
                ind_dict["Family"] = int(family)
                ind_dict["Family Size"] = periods_dict[period]["families"][family]["size"]
                ind_dict["Period"] = int(period)
                df.loc[len(df)] = ind_dict
end = time.time()