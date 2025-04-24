This folder contains some databases files in the JSON format.

The "orders.json" file contains the executed orders' data. It SHOULD be in this format*:
{
    "trades": [
        {"orderId": 3, "type": "bid" , "orderType": "market", "size": 614, "price": 56000000, "timestamp": 1725149122},
        ...
    ]
}

The "storicoOrdini.json" file is an example (possible instance) of "orders.json", but it's given by the assignment. Could be used to test the project by loading and executing these orders in the implemented orderbook system. This file doesn't respect the format*.

The "users.json" file contains the data used to authenticate the users. It SHOULD be in this format*:
[
    {"username":"exampleuser", "password":"examplepassword"},
    ...
]

The "defaultOrders.json" and the "defaultUsers.json" contain some initial templates to test / debug the project during its development.

That's because, by using EXACTLY these formats* (with the exactly positions of commas, lines, brackets), the project is optimized to append new users / orders without rewriting the whole files to avoid intensive I/O operations in case of large files. 

The "storicoOrdini.json" doesn't respect the format*, so can be used to load orders and tests, but cannot be used to write new executed orders on it, otherwise the program will fail.
