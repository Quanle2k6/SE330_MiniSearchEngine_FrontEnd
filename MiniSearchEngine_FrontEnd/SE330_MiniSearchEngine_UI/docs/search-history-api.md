# Search History API

Tài liệu này mô tả API lưu và lấy lịch sử tìm kiếm cho phía FE.

## Base URL

```txt
http://localhost:8080
```

Nếu BE chạy bằng port hoặc domain khác, FE thay `base URL` tương ứng theo môi trường.

## Định Dạng Response

Tất cả response thành công dạng object JSON sẽ được bọc theo format:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {}
}
```

Khi sử dụng dữ liệu, FE lấy payload chính trong field `data`.

## Enum

Field `type` có 2 giá trị:

| Giá trị | Ý nghĩa |
| --- | --- |
| `QUERY` | User tìm kiếm bằng từ khóa |
| `URL` | User click hoặc truy cập vào một kết quả/link |

## 1. Lưu Search History

```http
POST /search/history
Content-Type: application/json
```

### Lưu lịch sử loại QUERY

Dùng khi user submit từ khóa tìm kiếm.

Request:

```json
{
  "type": "QUERY",
  "query": "benh cam cum"
}
```

Response:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {
    "id": 1,
    "type": "QUERY",
    "visitedAt": "2026-06-13T09:24:24.918",
    "query": "benh cam cum",
    "title": null,
    "url": null
  }
}
```

### Lưu lịch sử loại URL

Dùng khi user click hoặc truy cập vào một link kết quả.

Request:

```json
{
  "type": "URL",
  "title": "Bài viết y tế",
  "url": "https://example.com/medical"
}
```

Response:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {
    "id": 2,
    "type": "URL",
    "visitedAt": "2026-06-13T09:24:25.022",
    "query": null,
    "title": "Bài viết y tế",
    "url": "https://example.com/medical"
  }
}
```

### Quy Tắc Validation

| Trường hợp | Bắt buộc |
| --- | --- |
| `type = QUERY` | `query` không được rỗng |
| `type = URL` | `title` và `url` không được rỗng |
| Mọi request | `type` bắt buộc có |

Nếu sai validation, BE trả HTTP `400`.

Ví dụ response lỗi:

```json
{
  "statusCode": 400,
  "error": "Illegal argument exception occurs...",
  "message": "query is required when type is QUERY",
  "data": null
}
```

## 2. Lấy Search History

```http
GET /search/history?page=0&size=10
```

Query params:

| Param | Kiểu dữ liệu | Mặc định | Ghi chú |
| --- | --- | --- | --- |
| `page` | number | `0` | Bắt đầu từ `0` |
| `size` | number | `10` | Số item mỗi page |

Response:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {
    "totalResults": 2,
    "totalPages": 1,
    "page": 0,
    "size": 10,
    "items": [
      {
        "id": 2,
        "type": "URL",
        "visitedAt": "2026-06-13T09:24:25.022",
        "query": null,
        "title": "Bài viết y tế",
        "url": "https://example.com/medical"
      },
      {
        "id": 1,
        "type": "QUERY",
        "visitedAt": "2026-06-13T09:24:24.918",
        "query": "benh cam cum",
        "title": null,
        "url": null
      }
    ]
  }
}
```

Kết quả được sắp xếp mới nhất trước theo `visitedAt DESC`.

## Gợi Ý Mapping Cho FE

FE có thể map mỗi item về shape đang dùng:

```ts
type SearchHistoryType = "QUERY" | "URL";

type SearchHistoryItem = {
  id: number;
  type: SearchHistoryType;
  visitedAt: string;
  query: string | null;
  title: string | null;
  url: string | null;
};
```

Hiển thị label:

```ts
const label =
  item.type === "QUERY"
    ? item.query
    : item.title;
```

Khi click item:

```ts
if (item.type === "QUERY") {
  // Fill search box bằng item.query và gọi API /search
}

if (item.type === "URL") {
  // Mở item.url
}
```

## Ví Dụ Fetch

### Lưu QUERY

```ts
await fetch(`${BASE_URL}/search/history`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    type: "QUERY",
    query,
  }),
});
```

### Lưu URL

```ts
await fetch(`${BASE_URL}/search/history`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    type: "URL",
    title,
    url,
  }),
});
```

### Lấy History

```ts
const res = await fetch(`${BASE_URL}/search/history?page=0&size=10`);
const body = await res.json();
const historyItems = body.data.items;
```
