This folder contains some databases.

The "storicoOrdini.json" file is given by the assignment to test the project. It SHOULD be in this format:
{
    "trades": [
        {"orderId": 3, "type": "bid" , "orderType": "market", "size": 614, "price": 56000000, "timestamp": 1725149122},
        ...
    ]
}

The "users.json" file contains the users' data. It SHOULD be in this format:
[
    {"username":"exampleuser","password":"examplepassword"},
    ...
]

That's because, by using these format, the project is optimized to append new users/orders without rewriting the whole file. If the files are .json, but not written in this format, the software when reading it to load the users/orders, will try to format them in this way. 
