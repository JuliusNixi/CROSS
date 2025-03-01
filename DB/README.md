This folder contains some databases files in the JSON format.

The "orders.json" file contains the executed orders' data. It SHOULD be in this format:
{
    "trades": [
        {"orderId": 3, "type": "bid" , "orderType": "market", "size": 614, "price": 56000000, "timestamp": 1725149122},
        ...
    ]
}

The "storicoOrdini.json" file is an example (possible instance) of "orders.json", but it's given by the assignment. Could be used to test the project by loading and executing these orders in the implemented orderbook system.

The "users.json" file contains the users' data. It SHOULD be in this format:
[
    {"username":"exampleuser", "password":"examplepassword"},
    ...
]

That's because, by using EXACTLY these formats, the project is optimized to append new users / orders without rewriting the whole files to avoid intensive I/O operations in case of large files. If the files are valid .json, but not written in this format (like the "storicoOrdini.json"), the software when reading it to load the users / orders, will try to format them in this way, rewriting them on the disk in a copy. 
