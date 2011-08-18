package com.MoofIT.Minecraft.Cenotaph;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.MoofIT.Minecraft.Cenotaph.Cenotaph.TombBlock;
import javax.swing.table.DefaultTableModel;

import org.bukkit.Location;

public class PailInterface extends JPanel {
	private static final long serialVersionUID = 4702240346538113734L;
	private Cenotaph plugin;

	public PailInterface(Cenotaph plugin) {
		this.plugin = plugin;
		initComponents();
	}

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        save = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Cenotaph"));
        setLayout(null);

        save.setText("Save");
        save.setFocusable(false);
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //saveActionPerformed(evt);
            }
        });
        add(save);
        save.setBounds(660, 380, 75, 29);

        model.setColumnIdentifiers(new String[] {"Player","Location","Security Left","Life Left"});
        table = new JTable(model);
        scrollPane = new JScrollPane(table);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setBounds(20, 178, 702, -150);
        add(scrollPane);
    }// </editor-fold>//GEN-END:initComponents
    private javax.swing.JButton save;
    private DefaultTableModel model;
    private JTable table;
    private JScrollPane scrollPane;

    public void updateCenotaphList() {
    	HashMap<String, ArrayList<TombBlock>> cenotaphList = plugin.getCenotaphList();
    	for (String Player : cenotaphList.keySet()) {
        	Cenotaph.log.info("[Cenotaph-Debug] Player: " + Player); //TODO DEBUG
    		for (TombBlock tBlock : cenotaphList.get(Player)) {
            	Cenotaph.log.info("[Cenotaph-Debug] Loc: " + tBlock.getBlock().getLocation().toString()); //TODO DEBUG
            	Location loc = tBlock.getBlock().getLocation();
    			model.addRow(new String[] {
    					tBlock.getOwner(),
    					"(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")", 
    					"sec left",
    					"life left"
    			});
    		}
    	}
    }
}