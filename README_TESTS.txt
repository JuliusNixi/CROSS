// testing general CLI
unknowncommand                                 // error, invalid command, no request sent
unknowncommand()                               // error, invalid command, no request sent
unknowncommand(arg)                            // error, invalid command, no request sent
unknowncommand(,)                              // error, invalid command, no request sent
()                                             // error, invalid command, no request sent
(                                              // error, invalid command, no request sent
,                                              // error, invalid command, no request sent
                                               // error, invalid command (only spaces), no request sent

// testing register
register()                                     // error, no args, no request sent
register(hello)                                // error, too few args, no request sent
register(hello,hello,hello)                    // error, too much args, no request sent
register(,,)                                   // error, empty args, no request sent
register(giulio, secret42)                     // error, user not avaible
register(pietro, a)                            // error, invalid password, too short
register(pietro, secret42)                     // ok

// testing login
login()                                        // error, no args, no request sent
login(hello)                                   // error, too few args, no request sent
login(hello,hello,hello)                       // error, too much args, no request sent
login(,,)                                      // error, empty args, no request sent
login(kenneth, secret)                         // error, user does not exist
login(giulio, secret42)                        // error, invalid password match
login(giulio, secret)                          // ok
login(giulio, secret)                          // error, user already logged in

// testing logout
logout()                                       // error, no args, no request sent
logout(hello, hello)                           // error, too much args, no request sent
logout(,)                                      // error, empty args, no request sent
logout(giulio)                                 // ok
logout(giulio)                                 // user not logged in

// testing update credentials
updatecredentials()                            // error, no args, no request sent
updatecredentials(hello)                       // error, too few args, no request sent
updatecredentials(hello, hello)                // error, too few args, no request sent
updatecredentials(hello, hello, hello, hello)  // error, too much args, no request sent
updatecredentials(,,)                          // error, empty args, no request sent
updatecredentials(giulio, secret, a)           // error, invalid new password, too short
updatecredentials(giulio, secret, secret)      // error, new password equals old one
login(giulio, secret)                          // ok
updatecredentials(giulio, secret, secret2)     // error, user currently logged in
logout(giulio)                                 // ok
updatecredentials(giulio, secret, secret2)     // ok
updatecredentials(giulio, secret2, secret)     // ok
login(giulio, secret)                          // ok
logout(giulio)                                 // ok

// testing only limit orders - using a new order book (restart the server) is recommended to get the same final book
insertlimitorder()                             // error, no args, no request sent
insertlimitorder(hello)                        // error, too few args, no request sent
insertlimitorder(hello, hello)                 // error, too few args, no request sent
insertlimitorder(hello, hello, hello, hello)   // error, too much args, no request sent
insertlimitorder(,,)                           // error, empty args, no request sent
insertlimitorder(lol, 10, 100)                 // error, invalid order direction (ask / bid), no request sent
insertlimitorder(ask, 10, 100)                 // error, not logged in
login(giulio, secret)                          // ok
insertlimitorder(ask, 0, 100)                  // error, invalid order quantity
insertlimitorder(ask, 1, 0)                    // error, invalid order price, no request sent
insertlimitorder(ask, 10, 100)                 // ok
insertlimitorder(ask, 2, 100)                  // ok, same price level of the previous (not empty price line)
insertlimitorder(ask, 1, 101)                  // ok, ask order up the best ask price
insertlimitorder(ask, 1, 99)                   // ok, less than best ask, new best ask creation
insertlimitorder(bid, 1, 100)                  // error, this order bid price > best ask
insertlimitorder(bid, 1, 99)                   // error, (with the assumption used in the project that the spread is at least 1) 
insertlimitorder(bid, 1, 95)                   // ok
insertlimitorder(bid, 1, 97)                   // ok, higher than best bid, new best bid creation
insertlimitorder(bid, 11, 95)                  // ok, same price level of the previous (not empty price line)
insertlimitorder(bid, 1, 94)                   // ok, bid order low the last bid price
logout(giulio)                                 // ok
// FINAL ORDER BOOK SHOULD BE:
// -----------------------------------------------------------------------------------------------
// LIMIT BOOK Pair [BTC/USD] - Actual Ask Value [99] - Actual Bid Value [97] - Price Increment [1]
// -----------------------------------------------------------------------------------------------
// [LimitOrder|ASK] - Price Value [101] - Line Size [1] - Total [101]
// [LimitOrder|ASK] - Price Value [100] - Line Size [12] - Total [1200]
// [LimitOrder|ASK] - Price Value [99] - Line Size [1] - Total [99]
// ****************************************************************
// [LimitOrder|BID] - Price Value [97] - Line Size [1] - Total [97]
// [LimitOrder|BID] - Price Value [95] - Line Size [12] - Total [1140]
// [LimitOrder|BID] - Price Value [94] - Line Size [1] - Total [94]
// -----------------------------------------------------------------------------------------------

// testing only stop orders - assuming the use of an orderbook with the previous only limit orders executed tests
insertstoporder()                             // error, no args, no request sent
insertstoporder(hello)                        // error, too few args, no request sent
insertstoporder(hello, hello)                 // error, too few args, no request sent
insertstoporder(hello, hello, hello, hello)   // error, too much args, no request sent
insertstoporder(,,)                           // error, empty args, no request sent
insertstoporder(lol, 10, 100)                 // error, invalid order direction (ask / bid), no request sent
insertstoporder(ask, 10, 100)                 // error, not logged in
login(giulio, secret)                         // ok
insertstoporder(ask, 0, 100)                  // error, invalid order quantity
insertstoporder(ask, 1, 0)                    // error, invalid order price, no request sent
insertstoporder(bid, 1, 99)                   // error, price equals to best ask
insertstoporder(bid, 1, 98)                   // error, price less than best ask
insertstoporder(bid, 1, 100)                  // ok
insertstoporder(bid, 2, 100)                  // ok, same price level of the previous (not empty price line)
insertstoporder(ask, 1, 95)                   // error, price equals to best bid
insertstoporder(ask, 1, 98)                   // error, price greater than best bid
insertstoporder(ask, 1, 94)                   // ok
insertstoporder(ask, 2, 94)                   // ok, same price level of the previous (not empty price line)
logout(giulio)                                // ok
// FINAL ORDER BOOK SHOULD BE:
// -------------------------------------------------------------------------------------------------------------------
// STOP BOOK (ASK/BID REVERSED) - Pair [BTC/USD] - Actual Ask Value [99] - Actual Bid Value [97] - Price Increment [1]
// -------------------------------------------------------------------------------------------------------------------
// [StopOrder|BID] - Price Value [100] - Line Size [3] - Total [300]
// ***************************************************************
// [StopOrder|ASK] - Price Value [95] - Line Size [1] - Total [95]
// [StopOrder|ASK] - Price Value [94] - Line Size [3] - Total [282]
// -------------------------------------------------------------------------------------------------------------------

// testing multiline limit and market orders - using a new order book (restart the server) is recommended to get the same final book
// testing single user
insertmarketorder()                           // error, no args, no request sent
insertmarketorder(hello)                      // error, too few args, no request sent
insertmarketorder(hello, hello, hello)        // error, too much args, no request sent
insertmarketorder(,)                          // error, empty args, no request sent
insertmarketorder(lol, 1)                     // error, invalid order direction (ask / bid), no request sent
insertmarketorder(ask, 1)                     // error, not logged in
login(giulio, secret)                         // ok
insertmarketorder(ask, 0)                     // error, invalid order quantity
insertlimitorder(ask, 12, 100)                // ok
insertlimitorder(ask, 2, 99)                  // ok
insertlimitorder(ask, 1, 98)                  // ok
insertmarketorder(bid, 5)                     // ok
insertlimitorder(bid, 1, 95)                  // ok
insertlimitorder(bid, 2, 94)                  // ok
insertlimitorder(bid, 12, 93)                 // ok
insertmarketorder(ask, 5)                     // ok
logout(giulio)                                // ok
// FINAL ORDER BOOK SHOULD BE:
// ------------------------------------------------------------------------------------------------
// LIMIT BOOK Pair [BTC/USD] - Actual Ask Value [100] - Actual Bid Value [93] - Price Increment [1]
// ------------------------------------------------------------------------------------------------
// [LimitOrder|ASK] - Price Value [100] - Line Size [10] - Total [1000]
// ******************************************************************
// [LimitOrder|BID] - Price Value [93] - Line Size [10] - Total [930]
// ------------------------------------------------------------------------------------------------

// testing multiline limit, stop and market orders - using a new order book (restart the server) is recommended to get the same final book
// testing single user
login(giulio, secret)                         // ok
insertlimitorder(ask, 1, 100)                 // ok
insertstoporder(bid, 1, 101)                  // ok
insertlimitorder(ask, 2, 102)                 // ok
insertmarketorder(bid, 1)                     // ok

// After the previous orders execution. To see the state of the Orders data structure in RAM we can use Orders.toStringOrders().

// FIRST GROUP:

// In the Orders data structure in RAM should be present among others (timestamp and id will be different):
// Order's Type [LimitOrder] - ID [105802696790875] - User [User [Username [giulio] - Password [secret] - FileLineID [13]]] - Price [Specific Price [Type [ASK] - Price Value [100] - Primary Currency [BTC] - Secondary Currency [USD]]] - Quantity [0] - Timestamp [1749058386761] - Initial Size [1]
// Order's Type [MarketOrder] - ID [105812692663416] - User [User [Username [giulio] - Password [secret] - FileLineID [13]]] - Execution Price [Specific Price [Type [ASK] - Price Value [100] - Primary Currency [BTC] - Secondary Currency [USD]]] - Quantity [0] - Timestamp [1749058386761]  - Initial Size [1] - Market Order Price Type [BID] - Coming From Stop Order ID [null]

// In the Orders databse file should be present among others (timestamp and id will be different):
// {"timestamp":1749058386761,"size":1,"orderType":"limit","type":"ask","price":100,"orderId":105802696790875},
// {"timestamp":1749058386761,"size":1,"orderType":"market","type":"bid","price":100,"orderId":105812692663416},

// SECOND GROUP:

// In the Orders data structure in RAM should be present among others (timestamp and id will be different):
// Order's Type [StopOrder] - ID [105806164913500] - User [User [Username [giulio] - Password [secret] - FileLineID [13]]] - Price [Specific Price [Type [BID] - Price Value [101] - Primary Currency [BTC] - Secondary Currency [USD]]] - Quantity [0] - Timestamp [1749058386783] - Initial Size [1]
// Order's Type [LimitOrder] - ID [105809239992083] - User [User [Username [giulio] - Password [secret] - FileLineID [13]]] - Price [Specific Price [Type [ASK] - Price Value [102] - Primary Currency [BTC] - Secondary Currency [USD]]] - Quantity [1] - Timestamp [1749058386783] - Initial Size [2]
// Order's Type [MarketOrder] - ID [105812716078708] - User [User [Username [giulio] - Password [secret] - FileLineID [13]]] - Execution Price [Specific Price [Type [ASK] - Price Value [102] - Primary Currency [BTC] - Secondary Currency [USD]]] - Quantity [0] - Timestamp [1749058386783]  - Initial Size [1] - Market Order Price Type [BID] - Coming From Stop Order ID [105806164913500]

// In the Orders databse file should be present among others (timestamp and id will be different):
// {"timestamp":1749058386783,"size":1,"orderType":"limit","type":"ask","price":102,"orderId":105809239992083},
// {"timestamp":1749058386783,"size":1,"orderType":"stop","type":"bid","price":102,"orderId":105806164913500}

// As we can see there are some differences between the data in the Orders data structure in RAM and the data in the Orders database file.
// This is due to implementation choices, note:
// Quantity: In the Orders data structure in RAM the quantity will have as its value the remaining part to be executed, or 0 if the order has been executed or readed from the Orders database file.
//           In the Orders database file will rapresent the opposite, so the executed part.
//           NOTE: The initial size is the quantity of the order when created or readed from the Orders database file.
// Market from Stop orders presence:
//           In the Orders data structure in RAM each executed stop order will become a market order, linked by the coming from stop order id field. The failed stop orders will not be saved.
//           In the Orders database file the market orders created as execution of stop orders are not saved.
// Stop orders prices:
//           In the Orders data structure in RAM the stop orders mantain the original price requested by the submitter.
//           In the Orders database file (and in the notifications) the stop orders show the market order price at which they were executed.

// bid side
insertlimitorder(bid, 1, 90)                     // ok
insertstoporder(ask, 1, 89)                      // ok
insertlimitorder(bid, 2, 88)                     // ok
insertmarketorder(ask, 1)                        // ok
logout(giulio)                                   // ok
// FINAL ORDER BOOK SHOULD BE:
// ------------------------------------------------------------------------------------------------
// LIMIT BOOK Pair [BTC/USD] - Actual Ask Value [102] - Actual Bid Value [88] - Price Increment [1]
// ------------------------------------------------------------------------------------------------
// [LimitOrder|ASK] - Price Value [102] - Line Size [1] - Total [102]
// ****************************************************************
// [LimitOrder|BID] - Price Value [88] - Line Size [1] - Total [88]
// ------------------------------------------------------------------------------------------------

// testing multiline limit, stop and market orders - using a new order book (restart the server) is recommended to get the same final book
// testing multiple users and notifications
login(giulio, secret)                           // ok, use a client
insertlimitorder(ask, 1, 100)                   // ok
insertstoporder(bid, 1, 101)                    // ok
login(alberto, secret6)                         // ok, use another client
insertlimitorder(ask, 2, 102)                   // ok
logout(alberto)                                 // ok, alberto should not see his notification, after, below, due to logout
login(fabrizio, secret22)                       // ok, use another client
insertmarketorder(bid, 1)                       // ok, 2 notification should arrive to giulio, 1 (0 seen) to alberto, 1 to fabrizio
// logout with all the users.
// FINAL ORDER BOOK SHOULD BE:
// --------------------------------------------------------------------------------------------------
// LIMIT BOOK Pair [BTC/USD] - Actual Ask Value [102] - Actual Bid Value [null] - Price Increment [1]
// --------------------------------------------------------------------------------------------------
// [LimitOrder|ASK] - Price Value [102] - Line Size [1] - Total [102]
// --------------------------------------------------------------------------------------------------

// testing unsatisfiable market order - using a new order book (restart the server) is recommended to get the same final book
login(giulio, secret)                            // ok
insertmarketorder(bid, 1)                        // error, empty book
insertlimitorder(ask, 1, 100)                    // ok
insertmarketorder(ask, 1)                        // error, empty book on this side
insertlimitorder(ask, 2, 100)                    // ok
insertmarketorder(bid, 5)                        // error, unsatisfiable
insertmarketorder(bid, 1)                        // ok
logout(giulio)                                   // ok
// FINAL ORDER BOOK SHOULD BE:
// --------------------------------------------------------------------------------------------------
// LIMIT BOOK Pair [BTC/USD] - Actual Ask Value [100] - Actual Bid Value [null] - Price Increment [1]
// --------------------------------------------------------------------------------------------------
// [LimitOrder|ASK] - Price Value [100] - Line Size [2] - Total [200]
// --------------------------------------------------------------------------------------------------

// testing unsatisfiable stop order - using a new order book (restart the server) is recommended to get the same final book
login(giulio, secret)                             // ok
insertlimitorder(ask, 1, 100)                     // ok
insertstoporder(bid, 5, 101)                      // ok
insertlimitorder(ask, 2, 102)                     // ok
insertmarketorder(bid, 1)                         // ok for this market order, but received notification for unsatisfiable previous submitted stop order
logout(giulio)                                    // ok
// FINAL ORDER BOOK SHOULD BE:
// --------------------------------------------------------------------------------------------------
// LIMIT BOOK Pair [BTC/USD] - Actual Ask Value [102] - Actual Bid Value [null] - Price Increment [1]
// --------------------------------------------------------------------------------------------------
// [LimitOrder|ASK] - Price Value [102] - Line Size [2] - Total [204]
// --------------------------------------------------------------------------------------------------

// testing cancel order - using a new order book (restart the server) is recommended to get the same final book
login(fabrizio, secret22)                         // ok
insertlimitorder(ask, 1, 99)                      // ok, get ID as ID1
logout(fabrizio)                                  // ok
login(giulio, secret)                             // ok
insertlimitorder(ask, 2, 100)                     // ok, get ID as ID2
insertlimitorder(ask, 1, 100)                     // ok
cancelorder()                                     // error, no args, no request sent
cancelorder(hello, hello)                         // error, too much args, no request sent
cancelorder(-1)                                   // error, order not exist
cancelorder(0)                                    // error, order not exist
cancelorder(42)                                   // error, present in Orders data structure in RAM (readed from Orders database file), but as already executed
logout(giulio)                                    // ok
cancelorder(ID2)                                  // error, not logged in
login(giulio, secret)                             // ok
cancelorder(ID1)                                  // error, order belongs to another user
cancelorder(ID2)                                  // ok
logout(giulio)                                    // ok
// FINAL ORDER BOOK SHOULD BE:
// -------------------------------------------------------------------------------------------------
// LIMIT BOOK Pair [BTC/USD] - Actual Ask Value [99] - Actual Bid Value [null] - Price Increment [1]
// -------------------------------------------------------------------------------------------------
// [LimitOrder|ASK] - Price Value [100] - Line Size [1] - Total [100]
// [LimitOrder|ASK] - Price Value [99] - Line Size [1] - Total [99]
// -------------------------------------------------------------------------------------------------

// testing get price history
getpricehistory()                                 // error, no args, no request sent
getpricehistory(hello, hello)                     // error, too much args, no request sent
getpricehistory(lol)                              // error, invalid month and year, no request sent
getpricehistory(132024)                           // error, invalid month, no request sent
getpricehistory(0105)                             // error, invalid year, no request sent
getpricehistory(052024)                           // ok, but no data presence for this request
getpricehistory(012025)                           // ok, login intentionally not required, as the request does not require user authentication
// EXPECTED RESPONSE SHOULD BE:
// ====== Price History Response ======
// Daily Price Stats Array:
//         Daily Price Stats [Daily GMT Date [2025-01-10 00:00:00 GMT], High Price Value [45], Low Price Value [45], Open Price Value [45], Close Price Value [45]]
//         Daily Price Stats [Daily GMT Date [2025-01-04 00:00:00 GMT], High Price Value [110], Low Price Value [90], Open Price Value [100], Close Price Value [105]]

// The last working get price history request with the default orders present in the Orders database file gives a response based on the following sample data:
// {"orderId": 45, "type": "bid" , "orderType": "limit", "size": 2, "price": 120, "timestamp": 1736190000},  // 6 jan 2025 19:00 not market
// {"orderId": 1, "type": "bid" , "orderType": "market", "size": 2, "price": 100, "timestamp": 1735714800},  // 4 jan 2025 07:00 open
// {"orderId": 44, "type": "ask" , "orderType": "market", "size": 2, "price": 75, "timestamp": 1736092800},  // 5 jan 2025 16:00 not bid
// {"orderId": 3, "type": "bid" , "orderType": "market", "size": 2, "price": 90, "timestamp": 1735981200},   // 4 jan 2025 14:00 low
// {"orderId": 42, "type": "bid" , "orderType": "market", "size": 2, "price": 115, "timestamp": 1704096000}, // 1 jan 2024 08:00 out of range year
// {"orderId": 2, "type": "bid" , "orderType": "market", "size": 2, "price": 110, "timestamp": 1736006400},  // 4 jan 2025 09:00 high
// {"orderId": 43, "type": "bid" , "orderType": "market", "size": 2, "price": 80, "timestamp": 1738407600},  // 1 jan 2025 08:00 out of range month
// {"orderId": 4, "type": "bid" , "orderType": "market", "size": 2, "price": 105, "timestamp": 1738360800},  // 4 jan 2025 22:00 close
// {"orderId": 44, "type": "bid" , "orderType": "stop", "size": 2, "price": 70, "timestamp": 1736010000}     // 4 jan 2025 17:00 not market
// {"orderId": 5, "type": "bid" , "orderType": "market", "size": 2, "price": 45, "timestamp": 1736499600}    // 10 jan 2025 09:00 open, close, low, high new day

// The price history response uses the following JSON structure:
{
    "priceHistory": [
        {
            "dayGMT": "2025-01-01 00:00:00 GMT",
            "high": 100,
            "low": 90,
            "open": 95,
            "close": 98
        }
    ]
}


