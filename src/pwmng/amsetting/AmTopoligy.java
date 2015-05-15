/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pwmng.amsetting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import pwmng.AMApplication;
import pwmng.AMApplication2;
import pwmng.AMApplication3;
import pwmng.AMComputeSys;
import pwmng.AMDataCenter;
import pwmng.AMEnterpriseSys;
import pwmng.AMEnterpriseSys2;
import pwmng.AMEnterpriseSys3;
import pwmng.AMGeneral;
import pwmng.AMInterUser;
import pwmng.AMInteractiveSys;
import pwmng.ComputeSystem;
import pwmng.DataCenter;
import pwmng.EnterpriseSystem;
import pwmng.GeneralSystem;
import pwmng.InteractiveSystem;
import pwmng.ui.VerticalLabelUI;

/**
 *
 * @author fnorouz
 */
public class AmTopoligy {

    static Class<?>[] amClasses = new Class<?>[]{
        AMApplication.class,
        AMApplication2.class,
        AMApplication3.class,
        AMComputeSys.class,
        AMDataCenter.class,
        AMEnterpriseSys2.class,
        AMEnterpriseSys3.class,
        AMEnterpriseSys.class,
        AMInterUser.class,
        AMInteractiveSys.class
    };

    public void readUI(JPanel panel1, JPanel panel2) {
    }

    public void updateMatrixUI(JPanel p2, int xoffset) {

        int maxwidth = 0, h = 20, w = 60;
        for (AMType aMType : moTypesWithAM) {
            JLabel l = new JLabel(aMType.getMoType());
            final Dimension s = l.getPreferredSize();
            if (s.width > maxwidth) {
                maxwidth = s.width;
            }
        }
        int x = maxwidth + 5;
        for (AMType aMType : moTypesWithAM) {
            JLabel label = new JLabel(aMType.getMoType());
            label.setUI(new VerticalLabelUI(true));
            final Dimension s = label.getPreferredSize();
            label.setBounds(xoffset + x, 0, s.width, s.height);
            p2.add(label);
            x += w;
        }
        int y = maxwidth + 5;
        for (final AMType aMType1 : moTypesWithAM) {
            JLabel label = new JLabel(aMType1.getMoType());
            final String string = aMType1.getMoType();
            final Dimension s = label.getPreferredSize();
            label.setBounds(xoffset, y, s.width, s.height);
            p2.add(label);
            x = maxwidth + 5;
            for (final AMType aMType2 : moTypesWithAM) {
                int index = 0;
                if (aMType1.getSubPrevillage().contains(aMType2)) {
                    index = 2;
                } else if (aMType1.getPair().contains(aMType2)) {
                    index = 1;
                }
                JComboBox combo = new JComboBox(new String[]{"N", "0", "1"});
                combo.setSelectedIndex(index);
                final Dimension size = combo.getPreferredSize();
                combo.setBounds(xoffset + x, y, w, h);
                x += w;
                combo.addItemListener(new ItemListener() {

                    public void itemStateChanged(ItemEvent e) {
                        final int selectedIndex = ((JComboBox) e.getSource()).getSelectedIndex();
                        if (selectedIndex == 0) {
                            aMType1.getSubPrevillage().remove(aMType2);
                            aMType1.getPair().remove(aMType2);
                        } else if (selectedIndex == 1) {
                            aMType1.getSubPrevillage().remove(aMType2);
                            aMType1.getPair().add(aMType2);
                        } else if (selectedIndex == 2) {
                            aMType1.getSubPrevillage().add(aMType2);
                            aMType1.getPair().remove(aMType2);
                        }
                    }
                });
                p2.add(combo);
            }
            y += h;
        }

        p2.repaint();
    }

    public void updateToUI(final JPanel panel1, final JPanel panel2) {
        JPanel p1 = new JPanel();
        p1.setLayout(null);
        JPanel p2 = new JPanel();
        p2.setLayout(null);
        panel1.removeAll();
        panel2.removeAll();
        panel1.setLayout(new BorderLayout());
        panel2.setLayout(new BorderLayout());

        int maxX = 0;
        int y = 0, h = 30;
        for (String string : availableMoTypes) {
            JLabel jLabe = new JLabel(string);
            final Dimension size = jLabe.getPreferredSize();
            if (maxX < size.getWidth()) {
                maxX = size.width;
            }
            jLabe.setBounds(0, y, (int) size.width, h);
            y += h;
            p2.add(jLabe);
        }
        y = 0;
        for (final String string : availableMoTypes) {
            boolean found = false;
            AMType amType = null;
            for (AMType aMType : moTypesWithAM) {
                if (aMType.getMoType().equals(string)) {
                    found = true;
                    amType = aMType;
                    break;
                }
            }
            JCheckBox check = new JCheckBox("", found);
            final Dimension size = check.getPreferredSize();
            check.setBounds((int) maxX + 5, y, size.width, h);
            check.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (((JCheckBox) e.getSource()).isSelected()) {
                        final AMType amType = new AMType();
                        amType.setMoType(string);
                        moTypesWithAM.add(amType);
                    } else {

                        for (AMType motype : moTypesWithAM) {
                            if (motype.getMoType().equals(string)) {
                                for (AMType aMType : moTypesWithAM) {
                                    aMType.getPair().remove(motype);
                                    aMType.getSubPrevillage().remove(motype);
                                }
                                moTypesWithAM.remove(motype);
                                break;
                            }
                        }

                    }
                    updateToUI(panel1, panel2);
                }
            });
            p2.add(check);

            Class<?> clazz = null;
            if (amType != null) {
                for (Class<?> class1 : amClasses) {
                    if (class1.getName().equals(amType.getAmJavaClass())) {
                        clazz = class1;
                        break;
                    }
                }
            }
            JComboBox combo = new JComboBox(amClasses);
            combo.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list, Object value, final int index, final boolean isSelected,
                        final boolean cellHasFocus) {

                    if (value != null) {
                        value = ((Class<?> )value).getSimpleName();
                    }

                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            combo.setSelectedItem(clazz);
            combo.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    final Class<?> clazz = (Class<?>) ((JComboBox) e.getSource()).getSelectedItem();
                    AMType amType = null;
                    for (AMType aMType : moTypesWithAM) {
                        if (aMType.getMoType().equals(string)) {
                            amType = aMType;
                            break;
                        }
                    }
                    if (amType != null) {
                        amType.setAmJavaClass(clazz.getName());
                    }
                }
            });
            combo.setBounds((int) maxX + 5 + 30, y, 150, h);
            p2.add(combo);
            y += h;
        }

        updateMatrixUI(p2, maxX + h + 50 + 180);
        p2.repaint();
        panel1.add(p1, BorderLayout.NORTH);
        panel2.add(p2, BorderLayout.CENTER);
    }

    public AmTopoligy() {
    }
    private List<String> availableMoTypes;
    private List<AMType> moTypesWithAM = new ArrayList<AMType>();

    public void setAvailableMos(ArrayList<InteractiveSystem> IS, ArrayList<EnterpriseSystem> ES, ArrayList<ComputeSystem> CS) {
        availableMoTypes = new ArrayList<String>();
        for (GeneralSystem system : CS) {
            if (!availableMoTypes.contains(system.getMoType())) {
                availableMoTypes.add(system.getMoType());
            }
        }
        for (EnterpriseSystem system : ES) {
            if (!availableMoTypes.contains(system.getMoType())) {
                availableMoTypes.add(system.getMoType());
                for (String string : system.getMoTypes()) {
                    if (!availableMoTypes.contains(string)) {
                        availableMoTypes.add(string);
                    }

                }
            }
        }
        for (GeneralSystem system : IS) {
            if (!availableMoTypes.contains(system.getMoType())) {
                availableMoTypes.add(system.getMoType());
            }
        }

        availableMoTypes.add("DataCenter");
    }

    public void parseXmlConfig(String config) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(config));
            // normalize text representation
            doc.getDocumentElement().normalize();
            readFromNode(doc.getDocumentElement());
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void readFromNode(Node node) {
        //if (ComputeNodeList.size()>0) ComputeNodeList.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("AMType")) {
                    final AMType amType = new AMType();
                    amType.readFromNode(childNodes.item(i));
                    moTypesWithAM.add(amType);
                } else if (childNodes.item(i).getNodeName().equalsIgnoreCase("Previllages")) {
                    final String relations = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    final String[] rows = relations.split(";");
                    for (int j = 0; j < rows.length; j++) {
                        final List<AMType> subPrevillage = new ArrayList<AMType>();
                        final List<AMType> pair = new ArrayList<AMType>();
                        String row = rows[j];
                        final String[] split = row.split(",");
                        for (int k = 0; k < split.length; k++) {
                            if (split[k].trim().equals("1")) {
                                subPrevillage.add(moTypesWithAM.get(k));

                            } else if (split[k].trim().equals("0")) {
                                pair.add(moTypesWithAM.get(k));
                            }
                        }
                        moTypesWithAM.get(j).setPair(pair);
                        moTypesWithAM.get(j).setSubPrevillage(subPrevillage);
                    }
                }
            }
        }
    }

    public void writeXmlConfig(String config) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            final Element createElement = doc.createElement("AmConfig");
            final Node appendChild = doc.appendChild(createElement);
            writeToNode(doc, appendChild);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(createElement);
            StreamResult result = new StreamResult(new File(config));

            //           Output to console for testing
//             StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(AmTopoligy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(AmTopoligy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void writeToNode(Document doc, Node node) {
        for (AMType amType : moTypesWithAM) {
            final Element createElement = doc.createElement("AMType");
            final Node appendChild = node.appendChild(createElement);
            amType.writeToNode(doc, appendChild);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moTypesWithAM.size(); i++) {
            for (int j = 0; j < moTypesWithAM.size(); j++) {
                if (moTypesWithAM.get(i).getSubPrevillage().contains(moTypesWithAM.get(j))) {
                    sb.append("1,");
                } else if (moTypesWithAM.get(i).getPair().contains(moTypesWithAM.get(j))) {
                    sb.append("0,");
                } else {
                    sb.append("N,");
                }
            }
            sb.append(";\n");
        }
        final Element createElement = doc.createElement("Previllages");
        final Text createTextNode = doc.createTextNode(sb.toString());
        node.appendChild(createElement);
        createElement.appendChild(createTextNode);
    }

    AMType getAmTypeForMoType(String moType) {
        return null;
    }

    public AMGeneral getAM(String moType, Object[] arg) {
        for (AMType aMType : moTypesWithAM) {
            if (aMType.getMoType().equals(moType)) {
                try {
                    AMGeneral am = (AMGeneral) Class.forName(aMType.getAmJavaClass()).getConstructors()[0].newInstance(arg);
                    aMType.getInstances().add(am);
                    am.setAmType(aMType);
                    return am;
                } catch (Exception ex) {
                    Logger.getLogger(AmTopoligy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
}
