### Observation监控指标

#### job执行状态统计

事件名称：job.status

触发时机：job执行完成时（包括执行成功或失败）

#### HighCardinalityKeyValues

| 属性  |类型| 备注                                                 |
| ------------ | ------------ |----------------------------------------------------|
| jobName | String  | job名字，可以自定义，如果未自定义是类名                              |
| status | enum | READY  STARTED  SUCCESS  FINISHED （非success都是执行失败） |


#### JFR

事件名称：Job Execute
事件分类（所属文件夹）：observation.task-center.job-execute

| 属性  | 备注  |
| ------------ |------------ |
| jobId  | job在初始化的时候创建的一个uuid(项目启动时候创建job) |
| jobName  | job名字，可以自定义，如果未自定义是类名 |
| cronExpression  | job的调度cron表达式 |
| status  | READY ---> STARTED ---> SUCCESS 或者 READY ---> STARTED ---> FINISHED|
