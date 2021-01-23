package hr.algebra.utils;

import hr.algebra.model.Orientation;
import hr.algebra.model.Player;
import hr.algebra.model.Position;
import hr.algebra.model.Snake;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author dnlbe
 */
public class DOMUtils {

    private static final String FILENAME_PLAYER = "players.xml";

    public static void savePlayer(Player player) {
        try {
            Document document = createDocument("players");
            document.getDocumentElement().appendChild(createPlayerElement(player, document));
            document.getDocumentElement().appendChild(createSnakeElement(player, document));
            saveDocument(document, FILENAME_PLAYER);
        } catch (TransformerException | ParserConfigurationException e) {
            Logger.getLogger(DOMUtils.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static Document createDocument(String root) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation domImplementation = builder.getDOMImplementation();
        return domImplementation.createDocument(null, root, null);
    }

    private static Node createPlayerElement(Player player, Document document) {
        Element element = document.createElement("player");
        element.setAttributeNode(createAttribute(document, "crashed", String.valueOf(player.isCrashed())));
        element.setAttributeNode(createAttribute(document, "orientation", String.valueOf(player.getOrientation())));
        return element;
    }

    private static Node createSnakeElement(Player player, Document document) {
        Element element = document.createElement("snake");
        element.setAttributeNode(createAttribute(document, "linewidth", String.valueOf(player.getSnake().getLineWidth())));
        element.appendChild(createPositionElements(document, "positions", player.getSnake().getPositions()));
        return element;
    }

    private static Node createPositionElements(Document document, String tagName, List<Position> positions) {
        Element element = document.createElement(tagName);
        positions.forEach(p -> element.appendChild(createPositionElement(document, "position", p)));
        return element;
    }

    private static Node createPositionElement(Document document, String name, Position position) {
        Element element = document.createElement(name);
        element.setAttributeNode(createAttribute(document, "x", String.valueOf(position.getX())));
        element.setAttributeNode(createAttribute(document, "y", String.valueOf(position.getY())));
        return element;
    }

    private static Attr createAttribute(Document document, String name, String value) {
        Attr attr = document.createAttribute(name);
        attr.setValue(value);
        return attr;
    }

    private static void saveDocument(Document document, String fileName) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(document), new StreamResult(new File(fileName)));
    }

    public static Player loadPlayer() {
        Player player = null;
        try {
            Document document = createDocument(new File(FILENAME_PLAYER));
            NodeList nodes = document.getElementsByTagName("players");
            for (int i = 0; i < nodes.getLength(); i++) {
                player = processPlayerNode((Element) nodes.item(i));
            }

        } catch (IOException | SAXException | ParserConfigurationException e) {
            Logger.getLogger(DOMUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return player;
    }

    private static Document createDocument(File file) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        return document;
    }

    private static Player processPlayerNode(Element element) {
        return new Player(
                processSnakeNode((Element) element.getElementsByTagName("snake").item(0)),
                processPlayerOrintation((Element) element.getElementsByTagName("player").item(0))
        );
    }

    private static Snake processSnakeNode(Element element) {
        return new Snake(
                Integer.valueOf(element.getAttribute("linewidth")),
                processPositionNodes(element.getElementsByTagName("position"))
        );
    }

    private static Orientation processPlayerOrintation(Element element) {
        return Orientation.valueOf(element.getAttribute("orientation"));
    }

    private static List<Position> processPositionNodes(NodeList nodes) {
        List<Position> positions = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            positions.add(processPositionNode((Element) nodes.item(i)));
        }

        return positions;
    }

    private static Position processPositionNode(Element element) {
        return new Position(
                Double.valueOf(element.getAttribute("x")),
                Double.valueOf(element.getAttribute("y"))
        );
    }
}
