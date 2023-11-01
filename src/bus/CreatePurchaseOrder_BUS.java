/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bus;

import dao.Product_DAO;
import dao.PurchaseOrderDetail_DAO;
import dao.PurchaseOrder_DAO;
import dao.Supplier_DAO;
import entity.Employee;
import entity.Product;
import entity.PurchaseOrder;
import entity.PurchaseOrderDetail;
import entity.Supplier;
import enums.PurchaseOrderStatus;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author thanhcanhit
 */
public class CreatePurchaseOrder_BUS {

    private final Product_DAO productDAO = new Product_DAO();
    private final Supplier_DAO suplierDAO = new Supplier_DAO();
    private final PurchaseOrder_DAO purchaseOrderDAO = new PurchaseOrder_DAO();
    private final PurchaseOrderDetail_DAO purchaseOrderDetailDAO = new PurchaseOrderDetail_DAO();

    public Product getProduct(String id) {
        return productDAO.getOne(id);
    }

    public ArrayList<Supplier> getAllSuplier() {
        return suplierDAO.getAll();
    }

    public PurchaseOrder createNewPurchaseOrder() throws Exception {
        PurchaseOrder order = new PurchaseOrder(purchaseOrderDAO.generateID());
        order.setStatus(PurchaseOrderStatus.PENDING);
        order.setEmployee(new Employee("NV001"));
        LocalDate now = LocalDate.now();
        order.setOrderDate(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return order;
    }

    public boolean saveToDatabase(PurchaseOrder order) {
        if (!purchaseOrderDAO.create(order)) {
            return false;
        }
        for (PurchaseOrderDetail detail : order.getPurchaseOrderDetailList()) {
            if (!purchaseOrderDetailDAO.create(detail)) {
                return false;
            }
        }

        return true;
    }

//    public boolean decreaseProductInventory(Product product, int quantity) {
//        int newInventory = product.getInventory() - quantity;
//        return productDAO.updateInventory(product.getProductID(), newInventory);
//    }
//
//    public boolean increaseProductInventory(Product product, int quantity) {
//        int newInventory = product.getInventory() + quantity;
//        return productDAO.updateInventory(product.getProductID(), newInventory);
//    }
}