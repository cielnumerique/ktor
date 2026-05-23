# Matrix Computation Service (Ktor)

HTTP-сервис для матричных вычислений, написанный на Kotlin + Ktor.

## Запуск

```bash
./gradlew run
```

## Endpoints

### `POST /compute`

Принимает задачу в JSON, немедленно возвращает `id` со статусом `PENDING`.

**Поддерживаемые задачи:**

| task | описание |
|------|----------|
| `matrix_multiply` | Умножение A × B |
| `matrix_transpose` | Транспонирование A |
| `determinant` | Определитель квадратной матрицы A |

**Пример запроса (транспонирование):**
```json
{
  "task": "matrix_transpose",
  "matrixA": [[1, 2, 3], [4, 5, 6]]
}
```

**Пример запроса (умножение):**
```json
{
  "task": "matrix_multiply",
  "matrixA": [[1, 2], [3, 4]],
  "matrixB": [[5, 6], [7, 8]]
}
```

**Ответ:**
```json
{ "id": "550e8400-e29b-41d4-a716-446655440000", "status": "PENDING" }
```

### `GET /result/{id}`

Возвращает результат вычисления.

**Ответ (DONE, матрица):**
```json
{
  "id": "550e8400-...",
  "status": "DONE",
  "task": "matrix_transpose",
  "result": {
    "type": "com.example.ResultData.Matrix",
    "matrix": [[1, 4], [2, 5], [3, 6]]
  }
}
```

**Ответ (ERROR):**
```json
{
  "id": "550e8400-...",
  "status": "ERROR",
  "task": "determinant",
  "error": "Matrix must be square"
}
```

## Архитектура

```
main.kt            — точка входа + Application.module()
Serialization.kt   — ContentNegotiation (JSON)
Routing.kt         — HTTP endpoints (POST /compute, GET /result/{id})
Models.kt          — DTO: ComputeRequest, SubmitResponse, ResultResponse, ResultData
ComputationEngine.kt — бизнес-логика, собственный CoroutineScope
```

## Используемые плагины Ktor

- `ContentNegotiation` + `kotlinx-serialization-json` — сериализация JSON
- `Netty` — HTTP-движок
- `YAML config` — конфигурация через `application.yaml`
