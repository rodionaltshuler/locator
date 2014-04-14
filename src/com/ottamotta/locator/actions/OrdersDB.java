package com.ottamotta.locator.actions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.contacts.ContactsModel;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.roboguice.LocatorInjector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * CRUD operations using SQLite for classes Order & Order.HistoryRecord
 */
public class OrdersDB extends SQLiteOpenHelper {

    public static final String DB_FILE_NAME = "LOCATOR_DB.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_ORDERS = "ORDERS";
    private static final String TABLE_ORDERS_HISTORY = "ORDERS_HISTORY";

    /**
     * ORDERS table fields
     */
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_STATUS = "_current_status";
    private static final String COLUMN_CONTACT = "_contact_id";
    private static final String COLUMN_COMMENT = "_comment";
    private static final String COLUMN_TYPE = "_type";
    private static final String COLUMN_TIME = "_time";
    private static final String COLUMN_TIME_PREV = "_time_prev";
    private static final String COLUMN_IS_REQUEST = "_is_request";
    private static final String COLUMN_NEED_LOCATION = "_need_location";
    private static final String COLUMN_LAT = "_lat";
    private static final String COLUMN_LON = "_lon";

    private static final String COLUMN_LAT_PREV = "_lat_prev";
    private static final String COLUMN_LON_PREV = "_lon_prev";

    // Database creation sql statement
    private static final String DATABASE_CREATE_ORDERS = "create table "
            + TABLE_ORDERS + "(" + COLUMN_ID + " INTEGER primary key autoincrement, "
            + COLUMN_STATUS + " INTEGER, "
            + COLUMN_CONTACT + " TEXT, "
            + COLUMN_COMMENT + " TEXT, "
            + COLUMN_TYPE + " INTEGER, "
            + COLUMN_TIME + " INTEGER, "
            + COLUMN_TIME_PREV + " INTEGER, "
            + COLUMN_IS_REQUEST + " INTEGER, "
            + COLUMN_NEED_LOCATION + " INTEGER, "
            + COLUMN_LAT_PREV + " REAL, "
            + COLUMN_LON_PREV + " REAL, "
            + COLUMN_LAT + " REAL, "
            + COLUMN_LON + " REAL);";

    private static final String DB_UPGRADE_ORDERS = "DROP TABLE IF EXISTS " + TABLE_ORDERS; //TODO ALTER TABLE

    private static final String COLUMN_HISTORY_ORDER_ID = "_order_id";
    private static final String COLUMN_HISTORY_TIME = "_time";
    private static final String COLUMN_HISTORY_NEW_STATUS = "_new_status";
    private static final String COLUMN_HISTORY_COMMENT = "_comment";

    private static final String DATABASE_CREATE_HISTORY = "create table "
            + TABLE_ORDERS_HISTORY + "(" + COLUMN_ID + " INTEGER primary key autoincrement, "
            + COLUMN_HISTORY_ORDER_ID + " INTEGER, "
            + COLUMN_HISTORY_TIME + " INTEGER, "
            + COLUMN_HISTORY_NEW_STATUS + " INTEGER, "
            + COLUMN_HISTORY_COMMENT + " TEXT);";

    private static final String DB_UPGRADE_HISTORY = "DROP TABLE IF EXISTS " + TABLE_ORDERS_HISTORY;

    private static final String TAG = "OrdersDB";

    private Context mContext;

    @Inject
    private ContactsModel contactsModel;

    public OrdersDB(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION);
        mContext = context;
        LocatorInjector.inject(this);
        //Log.d(TAG, "DB PATH: " + getWritableDatabase().getPath() + getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_ORDERS);
        db.execSQL(DATABASE_CREATE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(DB_UPGRADE_ORDERS);
        //db.execSQL(DB_UPGRADE_HISTORY);
        //onCreate(db);
        String[] columns = new String[] { COLUMN_TIME_PREV + " INTEGER",
                                          COLUMN_LAT_PREV + " REAL",
                                          COLUMN_LON_PREV + " REAL"};


        for (String query : columns) {
            String upgrade = "ALTER TABLE " + TABLE_ORDERS + " ADD " + query;
            db.execSQL(upgrade);
        }

    }

   /* @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //do nothing
    }*/

    public List<Order> getSavedOrders() {
        List<Order> orders = getEmptyOrders();
        Cursor c = getOrdersCursor();
        while (c.moveToNext()) {
            Order nextOrder = getOrderFromCursor(c);
            if (null != nextOrder) {
                orders.add(nextOrder);
            }
        }
        return orders;
    }

    public int clearOrders() {
        SQLiteDatabase db = getWritableDatabase();
        int rowsHistoryDeleted = db.delete(TABLE_ORDERS_HISTORY, null, null);
        int rowsDeleted = db.delete(TABLE_ORDERS, null, null);
        Log.d(TAG, "Clearing orders - deleted records: " + rowsDeleted);
        return rowsDeleted;
    }

    /**
     * @return -1 if error insterting record
     */
    public long createOrder(Order order) {

        long insertId = getWritableDatabase().insert(TABLE_ORDERS, null,
                getContentValuesFromOrder(order));

        if (insertId != -1) {
            order.setId(insertId);
        }
        assert insertId > -1;
        Log.d(TAG, "Inserted order for " + order.getContact().getName() + ", ID: " + insertId);

        createOrUpdateOrderHistory(order);

        return insertId;
    }

    /**
     * Update order status and history in db
     *
     * @param order
     * @return
     */
    public void updateOrder(Order order) {

        int rowsUpdated = getWritableDatabase().update(TABLE_ORDERS,
                getContentValuesFromOrder(order),
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(order.getId())});
        createOrUpdateOrderHistory(order);
        Log.d(TAG, "updating order #" + order.getId() + ", rows updated: " + rowsUpdated + "; now it has " + order.getHistory().size() + " history records");
        assert rowsUpdated == 1;

    }

    public void createOrUpdateOrderHistory(Order order) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            //FIXME update records, don't delete!
            //int deleted = db.delete(TABLE_ORDERS_HISTORY, COLUMN_HISTORY_ORDER_ID + " = ?", new String[]{String.valueOf(order.getId())});
            //Log.d(TAG, deleted + " order history records deleted for order #" + order.getId());


            for (Order.HistoryRecord rec : order.getHistory()) {

                Cursor exist = getReadableDatabase().query(TABLE_ORDERS_HISTORY, null,
                        COLUMN_ID + " = ?", new String[]{String.valueOf(rec.id)},
                        null, null, null);

                if (exist.getCount() == 0){
                    ContentValues values = getContentValuesFromHistoryRecord(rec);
                    rec.id = db.insert(TABLE_ORDERS_HISTORY, null, values);
                    Log.d(TAG, "History record #" + rec.id + " inserted");
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    public int deleteOrder(long orderId) {
        int rowsDeleted = getWritableDatabase().delete(TABLE_ORDERS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(orderId)});
        assert rowsDeleted == 1;
        return rowsDeleted;
    }

    public Order getSavedOrders(long orderId) {
        Cursor c = getOrdersCursor(orderId);
        if (c.getCount() > 0) {
            c.moveToFirst();
            return getOrderFromCursor(c);
        }
        return null;
    }

    private List<Order> getEmptyOrders() {
        return new ArrayList<>();
    }

    private ContentValues getContentValuesFromOrder(Order order) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMENT, "Order comment");
        values.put(COLUMN_STATUS, order.getCurrentStatus());
        values.put(COLUMN_TYPE, order.getStartAction().getType());
        values.put(COLUMN_CONTACT, order.getContact().getId());
        values.put(COLUMN_TIME, order.getStartAction().getTime());
        values.put(COLUMN_TIME_PREV, order.getStartAction().getPrevTime());
        if (order.getStartAction().getLocation() != null) {
            values.put(COLUMN_LAT, order.getStartAction().getLocation().latitude);
            values.put(COLUMN_LON, order.getStartAction().getLocation().longitude);
        }
        if (order.getStartAction().getPrevLocation() != null) {
            values.put(COLUMN_LAT_PREV, order.getStartAction().getPrevLocation().latitude);
            values.put(COLUMN_LON_PREV, order.getStartAction().getPrevLocation().longitude);
        }
        values.put(COLUMN_IS_REQUEST, order.getStartAction().isRequest());
        values.put(COLUMN_NEED_LOCATION, order.getStartAction().isNeedLocation());

        return values;
    }

    private Order getOrderFromCursor(Cursor values) {

        //PROBLEM HERE
        TrustedContact contact;
        contact = contactsModel.getContactById(values.getString(values.getColumnIndex(COLUMN_CONTACT)));

        if (contact == null) return null;

        Log.d(LocatorApplication.TAG, "order contains following contact: " + contact);
        //FIXME phone num to reply should be stored in DB
        //FIXME contact null here if it's a contact created by app -> getContactById is null ->
        Action startAction = new Action(contact.getMainPhoneNumber(), contact);

        startAction.setTime(values.getLong(values.getColumnIndex(COLUMN_TIME)));
        startAction.setType(values.getInt(values.getColumnIndex(COLUMN_TYPE)));

        assert contact != null;

        startAction.setContact(contact);
        startAction.setNeedLocation(values.getInt(values.getColumnIndex(COLUMN_NEED_LOCATION)) == 1);
        startAction.setRequest(values.getInt(values.getColumnIndex(COLUMN_IS_REQUEST)) == 1);
        double lat = values.getDouble(values.getColumnIndex(COLUMN_LAT));
        if (lat != 0) {
            double lon = values.getDouble(values.getColumnIndex(COLUMN_LON));
            LatLng location = new LatLng(lat, lon);
            startAction.setLocation(location);
        }

        double latPrev = values.getDouble(values.getColumnIndex(COLUMN_LAT_PREV));
        if (lat != 0) {
            double lonPrev = values.getDouble(values.getColumnIndex(COLUMN_LON_PREV));
            LatLng prevLocation = new LatLng(latPrev, lonPrev);
            startAction.setPrevLocation(prevLocation);
            startAction.setPrevTime(values.getLong(values.getColumnIndex(COLUMN_TIME_PREV)));
        }

        Order order = new Order(startAction);
        order.setId(values.getLong(values.getColumnIndex(COLUMN_ID)));
        order.currentStatus = values.getInt(values.getColumnIndex(COLUMN_STATUS));
        List<Order.HistoryRecord> historyRecords = getHistoryForOrder(order);

        for (Order.HistoryRecord rec : historyRecords) {
            order.getHistory().add(rec);
        }


        return order;
    }

    private List<Order.HistoryRecord> getHistoryForOrder(Order order) {
        List<Order.HistoryRecord> result = new ArrayList<>();
        Cursor c = getHistoryCursorForOrder(order.getId());
        while (c.moveToNext()) {
            Order.HistoryRecord nextRec = getHistoryRecordFromCursor(c);
            result.add(nextRec);
        }
        return result;
    }

    private Cursor getHistoryCursorForOrder(long orderId) {

        Cursor c = getReadableDatabase().query(TABLE_ORDERS_HISTORY, null,
                COLUMN_HISTORY_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)},
                null, null, null);

        //todo sort by time and then by status
        assert c.getCount() > 0;
        return c;
    }

    private Order.HistoryRecord getHistoryRecordFromCursor(Cursor cursor) {
        Order.HistoryRecord rec = new Order.HistoryRecord(
                cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_HISTORY_ORDER_ID)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_HISTORY_NEW_STATUS)),
                cursor.getString(cursor.getColumnIndex(COLUMN_HISTORY_COMMENT)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_HISTORY_TIME))
        );

        return rec;
    }

    private Cursor getOrdersCursor() {
        Cursor cursor = getWritableDatabase().query(TABLE_ORDERS, null, null, null, null, null, null);
        return cursor;
    }

    private Cursor getOrdersCursor(long orderId) {
        Cursor cursor = getWritableDatabase().query(TABLE_ORDERS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(orderId)}, null, null, null);
        return cursor;
    }

    private ContentValues getContentValuesFromHistoryRecord(Order.HistoryRecord rec) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HISTORY_ORDER_ID, rec.orderId);
        values.put(COLUMN_HISTORY_NEW_STATUS, rec.status);
        values.put(COLUMN_HISTORY_COMMENT, rec.comment);
        values.put(COLUMN_HISTORY_TIME, rec.time);
        return values;

    }

}
