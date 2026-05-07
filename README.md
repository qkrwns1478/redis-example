`POST http://localhost:8080/orders`

#### Request Body
```json
{
    "item": "mouse",
    "count": 5,
    "totalPrice": 10000,
    "status": "PENDING"
}
```

#### Response Body
```json
{
    "id": "42048c06-4308-40a3-894b-44d3796e8158",
    "item": "mouse",
    "count": 5,
    "totalPrice": 10000,
    "status": "PENDING"
}
```

