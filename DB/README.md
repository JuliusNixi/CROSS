This folder contains, in its subfolders, some databases files in the JSON format.
The project uses these files and thus the JSON format to handle persistent data about users and orders.

The "orders.json" file contains the executed orders' data. It's the file used in the project.
It SHOULD be in this format*:
{
    "trades": [
        {"orderId": 3, "type": "bid" , "orderType": "market", "size": 614, "price": 56000000, "timestamp": 1725149122},
        ...
    ]
}

The "storicoOrdini.json" file is an example (possible instance) of "orders.json", but it's given by the assignment. Could be used to test the project by loading and executing these orders in the implemented orderbook system. This file doesn't respect the format*.

The "users.json" file contains the data used to authenticate the registered users. It's the file used in the project.
It SHOULD be in this format*:
[
    {"username":"exampleuser", "password":"examplepassword"},
    ...
]

The "defaultOrders.json" and the "defaultUsers.json" contain some initial templates to test / debug the project during its development, thus they are possible instances of "users.json" and "orders.json".

The "users.json" and "orders.json" are the file used and modified by the project.

By using EXACTLY these formats* (with the exactly positions of commas, lines, brackets), the project is optimized to append new users / orders without rewriting the whole files to avoid intensive I/O operations in case of large files.

The "storicoOrdini.json" doesn't respect the format*, so can be used to load orders and test them, but cannot be used to write new executed orders on it, otherwise the program will fail, use "orders.json" instead.
