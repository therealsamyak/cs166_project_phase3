-- This appears in nearly every user-related function including LogIn(), viewProfile(), placeOrder(), and viewAllOrders().
CREATE INDEX idx_users_login ON Users (login);

-- The viewAllOrders() and viewRecentOrders() functions specifically filter on these columns.
CREATE INDEX idx_foodorder_login_status ON FoodOrder (login, orderStatus);

-- The viewRecentOrders() and viewAllOrders() functions sort by timestamp when displaying orders.
CREATE INDEX idx_foodorder_timestamp ON FoodOrder (orderTimestamp DESC);

-- The menu filtering functions frequently query items by type, as seen in the viewMenu() function.
CREATE INDEX idx_items_type ON Items (typeOfItem);

-- Many functions query the orderID in some fashion, whether in the viewRecent/All orders
-- or when placing / updating the orders too.
CREATE INDEX idx_itemsinorder_orderid ON ItemsInOrder (orderID);