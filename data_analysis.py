import pandas as pd
import json
periods_dict = json.load(open("EconomySimData.json"))["periods"]
df = pd.DataFrame(columns=["Period", "Family", "size", "utility", "good1", "good2"])
for period in periods_dict.keys():
    for family in periods_dict[period]["families"].keys():
        family_dict = periods_dict[period]["families"][family]
        family_dict["Family"] = int(family)
        family_dict["Period"] = int(period)
        df.loc[len(df)]  = family_dict
print(df.groupby("Period").sum()["size"].pct_change())



