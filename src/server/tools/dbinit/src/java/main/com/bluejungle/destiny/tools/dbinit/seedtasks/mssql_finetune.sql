CREATE STATISTICS [_dta_stat_552089453_17_2] 
  ON [RPA_LOG]([policy_decision], [time]);

CREATE NONCLUSTERED INDEX [RPA_LOG_FT_plt_INDEX] 
  ON [RPA_LOG] 
(
            [log_level] ASC,
            [policy_decision] ASC,
            [time] ASC
) INCLUDE ( [from_resource_name]) 
  WITH (SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) 
  ON [PRIMARY];

CREATE NONCLUSTERED INDEX [RPA_LOG_FT_lptim_INDEX] 
  ON [RPA_LOG] 
(
            [log_level] ASC,
            [policy_decision] ASC,
            [time] ASC,
            [id] ASC,
            [month_nb] ASC
) WITH (SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) 
  ON [PRIMARY];

CREATE STATISTICS [_dta_stat_552089453_2_1_19_17_3] 
  ON [RPA_LOG]([time], [id], [log_level], [policy_decision], [month_nb]);

CREATE STATISTICS [_dta_stat_552089453_3_19_17_2] 
  ON [RPA_LOG]([month_nb], [log_level], [policy_decision], [time]);

CREATE STATISTICS [_dta_stat_552089453_17_1_2] 
  ON [RPA_LOG]([policy_decision], [id], [time]);

CREATE STATISTICS [_dta_stat_552089453_1_3_2_19] 
  ON [RPA_LOG]([id], [month_nb], [time], [log_level]);

CREATE NONCLUSTERED INDEX [RPA_LOG_FT_lptp_INDEX] 
  ON [RPA_LOG] 
(
            [log_level] ASC,
            [policy_decision] ASC,
            [time] ASC,
            [policy_fullname] ASC
) WITH (SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) 
  ON [PRIMARY];

CREATE STATISTICS [_dta_stat_552089453_15_19_17_2] 
  ON [dbo].[RPA_LOG]([policy_fullname], [log_level], [policy_decision], [time]);


CREATE NONCLUSTERED INDEX [RPA_LOG_FT_lptu_INDEX] 
  ON [RPA_LOG] 
(
            [log_level] ASC,
            [policy_decision] ASC,
            [time] ASC,
            [user_name] ASC
) WITH (SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF) 
  ON [PRIMARY];


CREATE STATISTICS [_dta_stat_552089453_17_2] 
  ON [RPA_LOG]([policy_decision], [time]);

CREATE STATISTICS [_dta_stat_552089453_9_19_17] 
  ON [RPA_LOG]([user_name], [log_level], [policy_decision]);