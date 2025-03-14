DROP TRIGGER IF EXISTS update_order_price_after_item_change ON ItemsInOrder;

-- automatically calculate total order price
CREATE
OR REPLACE LANGUAGE plpgsql;

CREATE
OR REPLACE FUNCTION update_order_total_price () RETURNS "trigger" AS $BODY$ BEGIN
UPDATE FoodOrder
SET
    totalPrice = (
        SELECT
            COALESCE(SUM(i.price * io.quantity), 0)
        FROM
            ItemsInOrder io
            JOIN Items i ON io.itemName = i.itemName
        WHERE
            io.orderID = NEW.orderID
    )
WHERE
    orderID = NEW.orderID;

RETURN NEW;

END;

$BODY$ LANGUAGE plpgsql VOLATILE;

-- trigger
CREATE TRIGGER update_order_price_after_item_change AFTER INSERT
OR
UPDATE
OR DELETE ON ItemsInOrder FOR EACH ROW EXECUTE PROCEDURE update_order_total_price ();