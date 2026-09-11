# Study Hours 与 Big Picture 逻辑检查

## 一、Study Hours 涉及逻辑（是否接上）

### 数据源与持久化 ✅
- **Term.weeklyTargetMinutes**：默认 `createDefault()` 为 10 小时；可通过 `setWeeklyTargetMinutes()` 修改。
- **SqlDataSource**：`loadTerm()` 从表 `term` 的 `weekly_target_minutes` 读入；`saveTerm()` 通过 `upsertTermRow(term.getWeeklyTargetMinutes())` 写回。
- **Dashboard「Set weekly study hours」**：调用 `model.setWeeklyTargetMinutes(hours * 60)` → 更新内存中的 Term 并 `repository.saveTerm(term)`，已接上持久化。

### 计算层 ✅
- **StudyHoursService**：
  - `getWorkedMinutesThisWeek(sessions, now)`：按本周时间范围汇总 session 时长。
  - `getRemainingMinutes(sessions, weeklyTargetMinutes, now)`：`max(0, weeklyTargetMinutes - worked)`。
  - `getRemainingHHMM(term, sessions, clock)`：使用 `term.getWeeklyTargetMinutes()`，与当前 Term 一致。

### Model 层 ✅
- **Model.getRemainingHHMM(clock)**：内部用 `StudyHoursService.getRemainingHHMM(term, getWorkSessions(), clock)`。
- **Model.getRemainingMinutes(now)**：内部用 `StudyHoursService.getRemainingMinutes(..., term.getWeeklyTargetMinutes(), now)`。
- **Model.setWeeklyTargetMinutes(int)**：更新 `term.setWeeklyTargetMinutes` 并 `repository.saveTerm(term)`。  
→ 所有 study hours 计算都走同一个 Term 的 weekly target，已接上。

### 使用处连接情况

| 使用处 | 是否使用 weekly target / remaining | 说明 |
|--------|-----------------------------------|------|
| **Dashboard** | ✅ | `updateView()` 中 `remaining = model.getRemainingMinutes(clock.now())`，用于 `studyStatusColor()` 和 `studyStatusPercent()`；颜色和百分比与「本周剩余学习时长」一致。 |
| **Web API** `/api/study-hours` | ✅ | `handleStudyHours` 用 `model.getTerm()`、`model.getWorkSessions()` 和 `StudyHoursService.getRemainingHHMM(term, sessions, clock)`，返回的 `remainingHHMM` 与 Java 端 Model 一致。 |
| **前端 (main.js)** | ✅ | `loadStudyHours()` 请求 `/api/study-hours`，把 `d.remainingHHMM` 显示到 `#studyHoursRemaining`，与后端接上。 |
| **Term Page (TermView)** | — | 当前 Term 页没有「Study Hours Remaining」UI，未使用 study hours，无断接问题。 |

### 可选改进（语义更清晰）
- Dashboard 主文案是「Study hours left this week」，但**当前显示的数字**是「本周作业量」(`weekWorkloadHHMM`)，不是「本周剩余学习时长」(remaining)。  
- 若希望该处直接展示「剩余学习时长」，可改为用 `model.getRemainingHHMM(clock)` 作为主显示，或同时显示「剩余学习时长」和「本周作业量」。

---

## 二、Big Picture 逻辑

### 职责
- Big Picture 表示的是**作业待办量随时间的变化**（burn-down），与「每周学习目标小时数」是不同概念。
- 不依赖 `weeklyTargetMinutes` 或 `StudyHoursService` 是符合当前设计的。

### 数据流 ✅
- **BigPictureDataBuilder.build(model, clock)**：
  - 用 `model.getTerm()` 取课程/系列/作业；
  - 用 `model.getWorkSessions()` 取所有 work sessions；
  - 只计算「当前仍开放的作业」在某个时刻的**剩余预估分钟数**（estimated − worked），再换算成小时作为 Y 轴「Hours To-Do」。
- **IdealLine**：从图表起点时刻的「总剩余小时数」线性下降到 plotEnd 时为 0，表示理想 burn-down。
- **Segment**：每段是相邻事件时间点之间「总剩余小时」的阶梯变化，颜色按涉及课程，tooltip 为对应 work session。

### 与 Study Hours 的关系
- Big Picture **没有**使用「每周学习目标」或「本周已学时长」。
- 若产品上希望在 Big Picture 中体现「每周可学 X 小时」（例如一条水平参考线或说明文案），可以在现有逻辑上**额外**接入 `model.getTerm().getWeeklyTargetMinutes()` 做展示，而不影响现有 burn-down 逻辑。

---

## 三、结论

- **Study hours**：从 Term 的 weekly target、持久化、Model、Dashboard 颜色/百分比、到 API 和前端展示，整条链都接好了；唯一可改进点是 Dashboard 主数字目前是「本周作业量」而非「剩余学习时长」。
- **Big Picture**：逻辑自洽，只做「待办工时 burn-down」，未接 study hours 是设计选择；若要体现每周目标，可在此基础上增加展示即可。
