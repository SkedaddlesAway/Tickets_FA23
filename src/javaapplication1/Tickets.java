//Author: Gabi Bartolo
//makes gui window

package javaapplication1;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class Tickets extends JFrame implements ActionListener {

	// class level member objects
	Dao dao = new Dao(); // for CRUD operations
	String username; //seems handy
	Boolean ifAdmin = null; //permission setter
	JTable table = new JTable();


	// Main menu object items
	private JMenu mnuFile = new JMenu("File");
	private JMenu mnuAdmin = new JMenu("Admin");
	private JMenu mnuTickets = new JMenu("Tickets");

	// Sub menu item objects for all Main menu item objects
	JMenuItem mnuItemExit;
	JMenuItem mnuItemUpdate;
	JMenuItem mnuItemDelete;
	JMenuItem mnuItemOpenTicket;
	JMenuItem mnuItemViewTicket;
	JMenuItem mnuItemRefresh;

	public Tickets(Boolean isAdmin) {
		ifAdmin = isAdmin;
		createMenu();
		prepareGUI();
	}
	
	/*THINGS TO WORK ON AFTER BREAK
	 * - NORMAL VS ADMIN USER RIGHTS 
	 * - GUI DESIGN
	 * */

	private void createMenu() {
		/* Initialize sub menu items **************************************/
		// initialize sub menu item for File main menu
		mnuItemExit = new JMenuItem("Exit");
		// add to File main menu item
		mnuFile.add(mnuItemExit);
		
		//adding refresh button
		mnuItemRefresh = new JMenuItem("Refresh");
		// add to File main menu item
		mnuFile.add(mnuItemRefresh);

		if(ifAdmin) {// only add this menu + listening events if an admin logs in
			mnuItemUpdate = new JMenuItem("Update Ticket");
			mnuAdmin.add(mnuItemUpdate);

			mnuItemDelete = new JMenuItem("Delete Ticket");
			mnuAdmin.add(mnuItemDelete);

			mnuItemUpdate.addActionListener(this);
			mnuItemDelete.addActionListener(this);
		}

		// initialize first sub menu item for Tickets main menu
		mnuItemOpenTicket = new JMenuItem("Open Ticket");
		// add to Ticket Main menu item
		mnuTickets.add(mnuItemOpenTicket);

		// initialize second sub menu item for Tickets main menu
		mnuItemViewTicket = new JMenuItem("View Ticket");
		// add to Ticket Main menu item
		mnuTickets.add(mnuItemViewTicket);

		/* Add action listeners for each desired menu item *************/
		mnuItemExit.addActionListener(this);
		mnuItemOpenTicket.addActionListener(this);
		mnuItemViewTicket.addActionListener(this);

	}
	private void prepareGUI() {

		// create JMenu bar
		JMenuBar bar = new JMenuBar();
		bar.add(mnuFile); // add main menu items in order, to JMenuBar
		if(ifAdmin) 
			bar.add(mnuAdmin);
		bar.add(mnuTickets);
		// add menu bar components to frame
		setJMenuBar(bar);
		addWindowListener(new WindowAdapter() {
			// define a window close operation
			public void windowClosing(WindowEvent wE) {
				System.exit(0);
			}
		});
		
		// set frame options
		setSize(450, 450);
		getContentPane().setBackground(Color.black);
		setLocationRelativeTo(null);
		setVisible(true); //display table 
		
		try {
			table = new JTable(ticketsJTable.buildTableModel(dao.readRecords(username, ifAdmin)));
			table.setBounds(30, 40, 400, 400);
			JScrollPane sp = new JScrollPane(table);
			add(sp);
			setVisible(true); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// implement actions for sub menu items
		if (e.getSource() == mnuItemExit) {
			System.exit(0);
		} 
		//		else if (e.getSource() == mnuItemRefresh) {
		//			createMenu();
		//			prepareGUI();
		//		} scrapped for future tinkering
		else if (e.getSource() == mnuItemOpenTicket) {

			// get ticket information
			username = JOptionPane.showInputDialog(null, "Who is the issuer:");
			String ticketDesc = JOptionPane.showInputDialog(null, "Enter a ticket description:");

			// insert ticket information to database

			int id = dao.insertRecords(username, ticketDesc);

			// display results if successful or not to console / dialog box
			if (id != 0 && !username.equals("")) {
				System.out.println("Ticket #" + id + " created successfully!!!");
				JOptionPane.showMessageDialog(null, "Ticket #" + id + " created");
			} 
			else
				System.out.println("Ticket cannot be created!!!");
		}
		else if (e.getSource() == mnuItemViewTicket) {

			try {
				username = JOptionPane.showInputDialog(null, "Enter your username:");
				String ticketId = JOptionPane.showInputDialog(null, "Enter a ticket ID number:");
				JTable table = new JTable(ticketsJTable.buildTableModel(dao.ticketLookup(ifAdmin, username, Integer.parseInt(ticketId))));
				table.setBounds(30, 40, 200, 400);
				JScrollPane sp = new JScrollPane(table);
				add(sp);
				setVisible(true); // refreshes or repaints frame on screen

			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		
		//UPDATE TICKET updateRecords( boolean adm, String username, int ticketId, String ticketDesc, String status)
		else if (e.getSource() == mnuItemUpdate) {
			try {
				username = JOptionPane.showInputDialog(null, "Enter your username:");
				String ticketId = JOptionPane.showInputDialog(null, "Enter ticket ID:");
				String ticketDesc = JOptionPane.showInputDialog(null, "Enter ticket description:");
				String status = JOptionPane.showInputDialog(null, "Enter ticket status:");
				// run updateRecords
				dao.updateRecords(ifAdmin, username, Integer.parseInt(ticketId), ticketDesc, status);
				JOptionPane.showMessageDialog(null, "Ticket #" + ticketId + " was updated.");
				}
			catch(Exception se) {
				se.printStackTrace();
			}
		}
		
		//in theory, this option should only open for admins
		else if (e.getSource() == mnuItemDelete) {
			try {
				boolean confirm = false;
				while(!confirm) {
					username = JOptionPane.showInputDialog(null, "Enter your username:");
					String ticketId = JOptionPane.showInputDialog(null, "Enter ticket ID:");
					
					//Display record that's being deleted
					JTable table = new JTable(ticketsJTable.buildTableModel(dao.ticketLookup(ifAdmin, username, Integer.parseInt(ticketId))));
					table.setBounds(30, 40, 200, 400);
					JScrollPane sp = new JScrollPane(table);
					add(sp);
					setVisible(true); 
					
					//Make sure that's the record they want to delete
					String askForConfirm = JOptionPane.showInputDialog(null, "Delete the displayed ticket? <y/n>");
					if(askForConfirm.equalsIgnoreCase("y")) {
						confirm = true;
						dao.deleteRecords(Integer.parseInt(ticketId));
					}
					else
						JOptionPane.showMessageDialog(null, "Restarting operation...");
				}
			}
			catch(Exception se) {
				se.printStackTrace();
			}
		}

	}

}
