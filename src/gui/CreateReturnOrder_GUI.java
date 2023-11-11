/*
* Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
* Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
*/
package gui;

import bus.ReturnOrderManagament_BUS;
import com.formdev.flatlaf.FlatClientProperties;
import entity.Employee;
import entity.Order;
import entity.OrderDetail;
import entity.Product;
import entity.ReturnOrder;
import entity.ReturnOrderDetail;
import enums.ReturnOrderStatus;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.ButtonModel;
import javax.swing.table.DefaultTableModel;
import main.Application;
import raven.toast.Notifications;

/**
 *
 * @author Như Tâm
 */
public class CreateReturnOrder_GUI extends javax.swing.JPanel {
    
    private ReturnOrderManagament_BUS bus;
    private Order order;
    private Employee employee = Application.employee;
    private OrderDetail orderDetail;
    private DefaultTableModel tblModel_order;
    private DefaultTableModel tblModel_orderDetail;
    private DefaultTableModel tblModel_product;
    private ArrayList<ReturnOrderDetail> cart;
    private ButtonModel btnModel_type;
    private int maxQuantity;
    
    /**
     * Creates new form CreateReturnOrder_GUI
     */
    public CreateReturnOrder_GUI() {
        initComponents();
        init();
    }
    
    private void init() {
        bus = new ReturnOrderManagament_BUS();
        cart = new ArrayList<ReturnOrderDetail>();
        //model
        tblModel_order = new DefaultTableModel(new String[] {"Mã hoá đơn", "Mã khách hàng", "Ngày mua", "Tổng tiền"}, 0);
        tbl_order.setModel(tblModel_order);
        tblModel_orderDetail = new DefaultTableModel(new String[] {"Mã hoá đơn", "Mã sản phẩm", "Tên sản phẩm", "Số lượng", "Đơn giá", "Tổng tiền"}, 0);
        tbl_orderDetail.setModel(tblModel_orderDetail);
        tblModel_product = new DefaultTableModel(new String[]{"Mã sản phẩm", "Tên sản phẩm", "Số lượng"},0);
        tbl_product.setModel(tblModel_product);
        
        tbl_order.getSelectionModel().addListSelectionListener((e) -> {
            int rowIndex = tbl_order.getSelectedRow();
            
            if(rowIndex == -1)
                return;
            
            String orderID = tblModel_order.getValueAt(rowIndex, 0).toString();
            this.order = bus.getOrder(orderID);
            renderOrderDetail(orderID);
        });
        tbl_product.getModel().addTableModelListener((e) -> {
            int rowIndex = tbl_product.getSelectedRow();
            if(rowIndex == -1)
                return;
            if(Integer.parseInt(tblModel_product.getValueAt(rowIndex, 2).toString()) > maxQuantity) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Số lượng không vượt quá số lượng trong hoá đơn");
                tblModel_product.setValueAt(1, rowIndex, 2);
            }
        });
        //radio button
        renderCurrentEmployee();
        //renderOrderTable(bus.getAllOrder());
    }
    
    private void renderProductTable() throws Exception {
        tblModel_product.setRowCount(0);
        for (ReturnOrderDetail item : cart) {
            Object[] newRow = new Object[]{item.getProduct().getProductID(), item.getProduct().getName(), item.getQuantity()};
            tblModel_product.addRow(newRow);
        }
    }
    private void renderOrderDetail(String orderID) {
        tblModel_orderDetail.setRowCount(0);
        ArrayList<OrderDetail> orderDetailList = bus.getAllOrderDetail(orderID);
        for (OrderDetail orderDetail1 : orderDetailList) {
            String[] newRow = {orderDetail1.getOrder().getOrderID(), orderDetail1.getProduct().getProductID(), orderDetail1.getProduct().getName(), orderDetail1.getQuantity() + "", orderDetail1.getPrice() + "", orderDetail1.getLineTotal() + ""};
            tblModel_orderDetail.addRow(newRow);
        }
    }
    
    private void renderOrderTable(ArrayList<Order> orderList) {
        tblModel_order.setRowCount(0);
        for (Order order1 : orderList) {
            String[] newRow = {order1.getOrderID(), order1.getCustomer().getCustomerID(), order1.getOrderAt().toString(), order1.getTotalDue() + ""};
            tblModel_order.addRow(newRow);
            
        }
    }
    private void renderCurrentEmployee() {
        txt_employeeID.setText(employee.getEmployeeID());
        txt_nameEmp.setText(employee.getName());
    }
    private ReturnOrder getNewValues() {
        Date returnDate = chooseDateReturn.getDate();
        String returnOrderID = bus.generateID(returnDate);
        boolean type;
        if(rdb_return.isSelected())
            type = true;
        else
            type = false;
        return new ReturnOrder(returnDate, ReturnOrderStatus.PENDING, returnOrderID, employee, order, type);
    }
    private void handleAddItem(String productID, int quantityOrder) {
        maxQuantity = quantityOrder;
//        Kiểm tra xem trong giỏ hàng đã có sản phẩm đó hay chưa
        for (ReturnOrderDetail returnOrderDetail : cart) {
            if(returnOrderDetail.getProduct().getProductID().equals(productID)) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Sản phẩm đã được thêm");
                return;
            }
    }
//       Nếu chưa có thì thêm mới vào cart
        addItemToCart(productID, quantityOrder);
    }
        
    private void addItemToCart(String productID, int quantityOrder) {
        Product item = bus.getProduct(productID);
        if (item == null) {
            Notifications.getInstance().show(Notifications.Type.INFO, "Không tìm thấy sản phẩm có mã " + productID);
        } else {
            maxQuantity = quantityOrder;
            try {
//                Thêm vào giỏ hàng
                Product product = new Product(productID);
                ReturnOrderDetail newReturnOrderDetail = new ReturnOrderDetail(product, 1);
                cart.add(newReturnOrderDetail);
                renderProductTable();
                toggleChangeQuantity();
            } catch (Exception ex) {
                ex.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, "Có lỗi xảy ra khi thêm sản phẩm " + productID);
            }
        }
    }
    
    private void toggleChangeQuantity() {
        int row = cart.size() - 1;
        tbl_product.requestFocus();
        tbl_product.changeSelection(row, 2, false, false);
        tbl_product.setColumnSelectionInterval(2, 2);
        tbl_product.setRowSelectionInterval(row, row);
        tbl_product.editCellAt(row, 2);
        
    }
    
    private boolean validReturnOrder() {
        if(group_returnOrder.isSelected(btnModel_type)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng chọn loại đơn");
            return false;
        }
        if(tbl_product.getRowCount() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng thêm sản phẩm");
            return false;
        }
        return true;
    }
    
    private void createNewReturnOrder(ReturnOrder newReturnOrder) {
        if(bus.createNew(newReturnOrder)) {
            Notifications.getInstance().show(Notifications.Type.SUCCESS, "Thêm thành công");
            bus.createReturnOrderDetail(newReturnOrder, cart);
        }
        else
            Notifications.getInstance().show(Notifications.Type.ERROR, "Thêm không thành công");
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        group_returnOrder = new javax.swing.ButtonGroup();
        spl_createReturnOrder = new javax.swing.JSplitPane();
        pnl_order = new javax.swing.JPanel();
        pnl_searchOrder = new javax.swing.JPanel();
        txt_searchOrder = new javax.swing.JTextField();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(10, 10), new java.awt.Dimension(32767, 0));
        pnl_buttonSearchOrder = new javax.swing.JPanel();
        btn_searchOrder = new javax.swing.JButton();
        pnl_orderInfor = new javax.swing.JPanel();
        scr_order = new javax.swing.JScrollPane();
        tbl_order = new javax.swing.JTable();
        scr_orderDetail = new javax.swing.JScrollPane();
        tbl_orderDetail = new javax.swing.JTable();
        pnl_right = new javax.swing.JPanel();
        pnl_returnOrder = new javax.swing.JPanel();
        pnl_employeeInfor = new javax.swing.JPanel();
        pnl_employeeID = new javax.swing.JPanel();
        lbl_employeeID = new javax.swing.JLabel();
        txt_employeeID = new javax.swing.JTextField();
        pnl_nameEmp = new javax.swing.JPanel();
        lbl_nameEmp = new javax.swing.JLabel();
        txt_nameEmp = new javax.swing.JTextField();
        pnl_returnOrderInfor = new javax.swing.JPanel();
        pnl_returnOrderID = new javax.swing.JPanel();
        lbl_returnOrderID = new javax.swing.JLabel();
        txt_returnOrderID = new javax.swing.JTextField();
        pnl_orderID = new javax.swing.JPanel();
        lbl_orderID = new javax.swing.JLabel();
        txt_orderID = new javax.swing.JTextField();
        pnl_returnOrderDate = new javax.swing.JPanel();
        lbl_returnOrderDate = new javax.swing.JLabel();
        pnl_chooseDateReturn = new javax.swing.JPanel();
        chooseDateReturn = new com.toedter.calendar.JDateChooser();
        pnl_typeReturnOrder = new javax.swing.JPanel();
        lbl_typeReturnOrder = new javax.swing.JLabel();
        pnl_buttonTypeReturnOrder = new javax.swing.JPanel();
        rdb_return = new javax.swing.JRadioButton();
        rdb_exchange = new javax.swing.JRadioButton();
        pnl_productReturn = new javax.swing.JPanel();
        pnl_lblProduct = new javax.swing.JPanel();
        lbl_product = new javax.swing.JLabel();
        src_tblProduct = new javax.swing.JScrollPane();
        tbl_product = new javax.swing.JTable();
        pnl_createReturnOrder = new javax.swing.JPanel();
        btn_clearValue = new javax.swing.JButton();
        btn_addProduct = new javax.swing.JButton();
        btn_createReturnOrder = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        spl_createReturnOrder.setResizeWeight(0.95);

        pnl_order.setMinimumSize(new java.awt.Dimension(500, 123));
        pnl_order.setPreferredSize(new java.awt.Dimension(700, 768));
        pnl_order.setLayout(new java.awt.BorderLayout());

        pnl_searchOrder.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
        pnl_searchOrder.setMinimumSize(new java.awt.Dimension(457, 37));
        pnl_searchOrder.setLayout(new javax.swing.BoxLayout(pnl_searchOrder, javax.swing.BoxLayout.X_AXIS));

        txt_searchOrder.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã hoá đơn");
        txt_searchOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_searchOrderActionPerformed(evt);
            }
        });
        txt_searchOrder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txt_searchOrderKeyPressed(evt);
            }
        });
        pnl_searchOrder.add(txt_searchOrder);
        pnl_searchOrder.add(filler1);

        pnl_buttonSearchOrder.setMaximumSize(new java.awt.Dimension(50, 2147483647));
        pnl_buttonSearchOrder.setLayout(new java.awt.BorderLayout());

        btn_searchOrder.setText("Tìm kiếm");
        btn_searchOrder.putClientProperty(FlatClientProperties.STYLE,""
            + "background:$Menu.background;"
            + "foreground:$Menu.foreground;");
        btn_searchOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_searchOrderActionPerformed(evt);
            }
        });
        pnl_buttonSearchOrder.add(btn_searchOrder, java.awt.BorderLayout.CENTER);

        pnl_searchOrder.add(pnl_buttonSearchOrder);

        pnl_order.add(pnl_searchOrder, java.awt.BorderLayout.NORTH);

        pnl_orderInfor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Thông tin hoá đơn"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        pnl_orderInfor.setPreferredSize(new java.awt.Dimension(150, 700));
        pnl_orderInfor.setLayout(new java.awt.BorderLayout(0, 10));

        scr_order.setPreferredSize(new java.awt.Dimension(452, 200));

        tbl_order.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Mã HĐ", "Mã KH", "Ngày mua", "Tổng tiền"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scr_order.setViewportView(tbl_order);

        pnl_orderInfor.add(scr_order, java.awt.BorderLayout.PAGE_START);

        tbl_orderDetail.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mã HĐ", "Mã SP", "Tên SP", "SL", "Đơn giá", "Tổng tiền"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        scr_orderDetail.setViewportView(tbl_orderDetail);

        pnl_orderInfor.add(scr_orderDetail, java.awt.BorderLayout.CENTER);

        pnl_order.add(pnl_orderInfor, java.awt.BorderLayout.CENTER);

        spl_createReturnOrder.setLeftComponent(pnl_order);

        pnl_right.setMaximumSize(new java.awt.Dimension(600, 2147483647));
        pnl_right.setMinimumSize(new java.awt.Dimension(500, 123));
        pnl_right.setLayout(new java.awt.BorderLayout());

        pnl_returnOrder.setMaximumSize(new java.awt.Dimension(600, 768));
        pnl_returnOrder.setMinimumSize(new java.awt.Dimension(400, 123));
        pnl_returnOrder.setPreferredSize(new java.awt.Dimension(400, 768));
        pnl_returnOrder.setLayout(new javax.swing.BoxLayout(pnl_returnOrder, javax.swing.BoxLayout.Y_AXIS));

        pnl_employeeInfor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Nhân viên"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        pnl_employeeInfor.setMaximumSize(new java.awt.Dimension(2147483647, 130));
        pnl_employeeInfor.setMinimumSize(new java.awt.Dimension(400, 113));
        pnl_employeeInfor.setPreferredSize(new java.awt.Dimension(30, 120));
        pnl_employeeInfor.setLayout(new javax.swing.BoxLayout(pnl_employeeInfor, javax.swing.BoxLayout.Y_AXIS));

        pnl_employeeID.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnl_employeeID.setLayout(new javax.swing.BoxLayout(pnl_employeeID, javax.swing.BoxLayout.LINE_AXIS));

        lbl_employeeID.setText("Mã nhân viên:");
        pnl_employeeID.add(lbl_employeeID);

        txt_employeeID.setMaximumSize(new java.awt.Dimension(2147483647, 30));
        txt_employeeID.setMinimumSize(new java.awt.Dimension(64, 30));
        txt_employeeID.setPreferredSize(new java.awt.Dimension(64, 30));
        pnl_employeeID.add(txt_employeeID);

        pnl_employeeInfor.add(pnl_employeeID);

        pnl_nameEmp.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnl_nameEmp.setLayout(new javax.swing.BoxLayout(pnl_nameEmp, javax.swing.BoxLayout.X_AXIS));

        lbl_nameEmp.setText("Tên nhân viên:");
        pnl_nameEmp.add(lbl_nameEmp);

        txt_nameEmp.setMaximumSize(new java.awt.Dimension(2147483647, 30));
        txt_nameEmp.setMinimumSize(new java.awt.Dimension(64, 30));
        txt_nameEmp.setPreferredSize(new java.awt.Dimension(64, 30));
        pnl_nameEmp.add(txt_nameEmp);

        pnl_employeeInfor.add(pnl_nameEmp);

        pnl_returnOrder.add(pnl_employeeInfor);

        pnl_returnOrderInfor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Thông tin đơn đổi trả"), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        pnl_returnOrderInfor.setMinimumSize(new java.awt.Dimension(400, 278));
        pnl_returnOrderInfor.setPreferredSize(new java.awt.Dimension(400, 100));
        pnl_returnOrderInfor.setLayout(new javax.swing.BoxLayout(pnl_returnOrderInfor, javax.swing.BoxLayout.Y_AXIS));

        pnl_returnOrderID.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        pnl_returnOrderID.setMaximumSize(new java.awt.Dimension(2147483647, 45));
        pnl_returnOrderID.setMinimumSize(new java.awt.Dimension(118, 45));
        pnl_returnOrderID.setPreferredSize(new java.awt.Dimension(597, 34));
        pnl_returnOrderID.setLayout(new javax.swing.BoxLayout(pnl_returnOrderID, javax.swing.BoxLayout.X_AXIS));

        lbl_returnOrderID.setText("Mã HĐĐT:");
        lbl_returnOrderID.setMaximumSize(new java.awt.Dimension(85, 16));
        lbl_returnOrderID.setMinimumSize(new java.awt.Dimension(85, 16));
        lbl_returnOrderID.setPreferredSize(new java.awt.Dimension(85, 16));
        pnl_returnOrderID.add(lbl_returnOrderID);

        txt_returnOrderID.setMaximumSize(new java.awt.Dimension(2147483647, 30));
        txt_returnOrderID.setMinimumSize(new java.awt.Dimension(64, 30));
        txt_returnOrderID.setPreferredSize(new java.awt.Dimension(64, 30));
        pnl_returnOrderID.add(txt_returnOrderID);

        pnl_returnOrderInfor.add(pnl_returnOrderID);

        pnl_orderID.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        pnl_orderID.setMaximumSize(new java.awt.Dimension(2147483647, 45));
        pnl_orderID.setMinimumSize(new java.awt.Dimension(118, 45));
        pnl_orderID.setPreferredSize(new java.awt.Dimension(597, 34));
        pnl_orderID.setLayout(new javax.swing.BoxLayout(pnl_orderID, javax.swing.BoxLayout.X_AXIS));

        lbl_orderID.setText("Mã hoá đơn:");
        lbl_orderID.setMaximumSize(new java.awt.Dimension(85, 16));
        lbl_orderID.setMinimumSize(new java.awt.Dimension(85, 16));
        lbl_orderID.setPreferredSize(new java.awt.Dimension(85, 16));
        pnl_orderID.add(lbl_orderID);

        txt_orderID.setMaximumSize(new java.awt.Dimension(2147483647, 30));
        txt_orderID.setMinimumSize(new java.awt.Dimension(64, 30));
        txt_orderID.setPreferredSize(new java.awt.Dimension(64, 30));
        pnl_orderID.add(txt_orderID);

        pnl_returnOrderInfor.add(pnl_orderID);

        pnl_returnOrderDate.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        pnl_returnOrderDate.setMaximumSize(new java.awt.Dimension(2147483647, 45));
        pnl_returnOrderDate.setMinimumSize(new java.awt.Dimension(132, 45));
        pnl_returnOrderDate.setPreferredSize(new java.awt.Dimension(132, 50));
        pnl_returnOrderDate.setLayout(new javax.swing.BoxLayout(pnl_returnOrderDate, javax.swing.BoxLayout.X_AXIS));

        lbl_returnOrderDate.setText("Ngày đổi trả:");
        pnl_returnOrderDate.add(lbl_returnOrderDate);

        pnl_chooseDateReturn.setMaximumSize(new java.awt.Dimension(32767, 30));
        pnl_chooseDateReturn.setPreferredSize(new java.awt.Dimension(100, 30));
        pnl_chooseDateReturn.setLayout(new java.awt.GridLayout(1, 0));
        pnl_chooseDateReturn.add(chooseDateReturn);

        pnl_returnOrderDate.add(pnl_chooseDateReturn);

        pnl_returnOrderInfor.add(pnl_returnOrderDate);

        pnl_typeReturnOrder.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        pnl_typeReturnOrder.setMaximumSize(new java.awt.Dimension(32792, 34));
        pnl_typeReturnOrder.setPreferredSize(new java.awt.Dimension(1191, 34));
        pnl_typeReturnOrder.setLayout(new javax.swing.BoxLayout(pnl_typeReturnOrder, javax.swing.BoxLayout.X_AXIS));

        lbl_typeReturnOrder.setText("Loại:");
        lbl_typeReturnOrder.setPreferredSize(lbl_returnOrderDate.getPreferredSize());
        pnl_typeReturnOrder.add(lbl_typeReturnOrder);

        pnl_buttonTypeReturnOrder.setMaximumSize(new java.awt.Dimension(32767, 30));
        pnl_buttonTypeReturnOrder.setMinimumSize(new java.awt.Dimension(155, 30));
        pnl_buttonTypeReturnOrder.setPreferredSize(new java.awt.Dimension(155, 30));
        pnl_buttonTypeReturnOrder.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        group_returnOrder.add(rdb_return);
        rdb_return.setText("Trả hàng");
        pnl_buttonTypeReturnOrder.add(rdb_return);

        group_returnOrder.add(rdb_exchange);
        rdb_exchange.setText("Đổi hàng");
        pnl_buttonTypeReturnOrder.add(rdb_exchange);

        pnl_typeReturnOrder.add(pnl_buttonTypeReturnOrder);

        pnl_returnOrderInfor.add(pnl_typeReturnOrder);

        pnl_productReturn.setLayout(new javax.swing.BoxLayout(pnl_productReturn, javax.swing.BoxLayout.Y_AXIS));

        pnl_lblProduct.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnl_lblProduct.setMaximumSize(new java.awt.Dimension(2147483647, 34));
        pnl_lblProduct.setPreferredSize(new java.awt.Dimension(10, 34));
        pnl_lblProduct.setLayout(new java.awt.BorderLayout());

        lbl_product.setText("Sản phẩm");
        lbl_product.setMaximumSize(new java.awt.Dimension(53, 30));
        lbl_product.setMinimumSize(new java.awt.Dimension(53, 30));
        lbl_product.setPreferredSize(new java.awt.Dimension(53, 30));
        pnl_lblProduct.add(lbl_product, java.awt.BorderLayout.CENTER);

        pnl_productReturn.add(pnl_lblProduct);

        src_tblProduct.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        tbl_product.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Mã SP", "Tên SP", "SL"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tbl_product.setMinimumSize(new java.awt.Dimension(45, 60));
        src_tblProduct.setViewportView(tbl_product);
        if (tbl_product.getColumnModel().getColumnCount() > 0) {
            tbl_product.getColumnModel().getColumn(2).setResizable(false);
        }

        pnl_productReturn.add(src_tblProduct);

        pnl_returnOrderInfor.add(pnl_productReturn);

        pnl_createReturnOrder.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        pnl_createReturnOrder.setPreferredSize(new java.awt.Dimension(1191, 50));
        pnl_createReturnOrder.setLayout(new java.awt.GridLayout());

        btn_clearValue.setText("XOÁ TRẮNG");
        pnl_createReturnOrder.add(btn_clearValue);

        btn_addProduct.setText("THÊM SẢN PHẨM");
        btn_addProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_addProductActionPerformed(evt);
            }
        });
        pnl_createReturnOrder.add(btn_addProduct);

        btn_createReturnOrder.setText("TẠO ĐƠN ĐỔI TRẢ");
        btn_createReturnOrder.setPreferredSize(new java.awt.Dimension(129, 40));
        btn_createReturnOrder.putClientProperty(FlatClientProperties.STYLE,""
            + "background:$Menu.background;"
            + "foreground:$Menu.foreground;");
        btn_createReturnOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_createReturnOrderActionPerformed(evt);
            }
        });
        pnl_createReturnOrder.add(btn_createReturnOrder);

        pnl_returnOrderInfor.add(pnl_createReturnOrder);

        pnl_returnOrder.add(pnl_returnOrderInfor);

        pnl_right.add(pnl_returnOrder, java.awt.BorderLayout.CENTER);

        spl_createReturnOrder.setRightComponent(pnl_right);

        add(spl_createReturnOrder, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btn_createReturnOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_createReturnOrderActionPerformed
        if(!validReturnOrder()) {
            Notifications.getInstance().show(Notifications.Type.INFO, "Vui lòng nhập dữ liệu");
            return;
        }
        else {
            ReturnOrder newReturnOrder = getNewValues();
        createNewReturnOrder(newReturnOrder);
        }
        
    }//GEN-LAST:event_btn_createReturnOrderActionPerformed
    
    private void btn_addProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_addProductActionPerformed
        int rowIndex = tbl_orderDetail.getSelectedRow();
        String productID = tblModel_orderDetail.getValueAt(rowIndex, 1).toString();
        int quantityOrder = Integer.parseInt(tblModel_orderDetail.getValueAt(rowIndex, 3).toString());
        handleAddItem(productID, quantityOrder);
        
    }//GEN-LAST:event_btn_addProductActionPerformed
    
    private void btn_searchOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_searchOrderActionPerformed
        String orderID = txt_searchOrder.getText();
        if(orderID.isBlank()) {
            Notifications.getInstance().show(Notifications.Type.INFO, "Vui lòng nhập mã hoá đơn để tìm");
            return;
        } else
            renderOrderTable(bus.searchByOrderId(orderID));
    }//GEN-LAST:event_btn_searchOrderActionPerformed

    private void txt_searchOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_searchOrderActionPerformed

    }//GEN-LAST:event_txt_searchOrderActionPerformed

    private void txt_searchOrderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchOrderKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String orderID = txt_searchOrder.getText();
            if(orderID.isBlank()) {
                Notifications.getInstance().show(Notifications.Type.INFO, "Vui lòng nhập mã hoá đơn để tìm");
                return;
            } else
                renderOrderTable(bus.searchByOrderId(orderID));
        }
    }//GEN-LAST:event_txt_searchOrderKeyPressed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_addProduct;
    private javax.swing.JButton btn_clearValue;
    private javax.swing.JButton btn_createReturnOrder;
    private javax.swing.JButton btn_searchOrder;
    private com.toedter.calendar.JDateChooser chooseDateReturn;
    private javax.swing.Box.Filler filler1;
    private javax.swing.ButtonGroup group_returnOrder;
    private javax.swing.JLabel lbl_employeeID;
    private javax.swing.JLabel lbl_nameEmp;
    private javax.swing.JLabel lbl_orderID;
    private javax.swing.JLabel lbl_product;
    private javax.swing.JLabel lbl_returnOrderDate;
    private javax.swing.JLabel lbl_returnOrderID;
    private javax.swing.JLabel lbl_typeReturnOrder;
    private javax.swing.JPanel pnl_buttonSearchOrder;
    private javax.swing.JPanel pnl_buttonTypeReturnOrder;
    private javax.swing.JPanel pnl_chooseDateReturn;
    private javax.swing.JPanel pnl_createReturnOrder;
    private javax.swing.JPanel pnl_employeeID;
    private javax.swing.JPanel pnl_employeeInfor;
    private javax.swing.JPanel pnl_lblProduct;
    private javax.swing.JPanel pnl_nameEmp;
    private javax.swing.JPanel pnl_order;
    private javax.swing.JPanel pnl_orderID;
    private javax.swing.JPanel pnl_orderInfor;
    private javax.swing.JPanel pnl_productReturn;
    private javax.swing.JPanel pnl_returnOrder;
    private javax.swing.JPanel pnl_returnOrderDate;
    private javax.swing.JPanel pnl_returnOrderID;
    private javax.swing.JPanel pnl_returnOrderInfor;
    private javax.swing.JPanel pnl_right;
    private javax.swing.JPanel pnl_searchOrder;
    private javax.swing.JPanel pnl_typeReturnOrder;
    private javax.swing.JRadioButton rdb_exchange;
    private javax.swing.JRadioButton rdb_return;
    private javax.swing.JScrollPane scr_order;
    private javax.swing.JScrollPane scr_orderDetail;
    private javax.swing.JSplitPane spl_createReturnOrder;
    private javax.swing.JScrollPane src_tblProduct;
    private javax.swing.JTable tbl_order;
    private javax.swing.JTable tbl_orderDetail;
    private javax.swing.JTable tbl_product;
    private javax.swing.JTextField txt_employeeID;
    private javax.swing.JTextField txt_nameEmp;
    private javax.swing.JTextField txt_orderID;
    private javax.swing.JTextField txt_returnOrderID;
    private javax.swing.JTextField txt_searchOrder;
    // End of variables declaration//GEN-END:variables
    
}
