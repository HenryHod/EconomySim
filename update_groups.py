import sqlite3
import pandas as pd
childcost = 20
conn = sqlite3.connect(r"simulation.db")
cursor = conn.cursor()
cursor.execute("""
                DROP TABLE IF EXISTS groups""")
cursor.execute(f"""CREATE TABLE groups AS
                        SELECT 
                            (CAST(SUM(children - prev_children) AS DOUBLE) / COUNT(id)) * 100 AS pop_growth,
                            (CAST(future_goods AS DOUBLE) / goods) * 100 AS savings_rate,
                            ((CAST(self_goods AS DOUBLE) + (children - prev_children) * {childcost}) / goods) * 100 AS cons_rate,
                            (CAST(char_goods AS DOUBLE) / goods) * 100 AS char_rate,
                            mean_altruism, 
                            mean_patience,
                            mean_charity, 
                            std,
                            period,
                            sim_id FROM simulations
                        WHERE std == 0.05 AND age > 0
                        GROUP BY sim_id, period, mean_altruism, mean_patience, mean_charity, std
                        
                        """)