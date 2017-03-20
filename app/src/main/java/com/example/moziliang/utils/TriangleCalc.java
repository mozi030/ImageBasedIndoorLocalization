package com.example.moziliang.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;

import com.example.moziliang.indoorlocalizationrelease.NonfreeJNILib;


public class TriangleCalc {

	@SuppressLint("SdCardPath")
	private static String map_xml = "/sdcard/data/manyImages/data_v2/mall.xml";

	static private Map<Integer, Position> stores_num_and_position = new HashMap<Integer, Position>();
	static private Map<Integer, String> stores_num_and_name = new HashMap<Integer, String>();

	static private double pi = 3.141592654;

	static public double[] calculate(int storeNum1, int storeNum2,
			int storeNum3, double angle1, double angle2, double angle3) {

		if (stores_num_and_position.size() == 0) {
			getMapXml();
		}

		Position position1 = stores_num_and_position.get(storeNum1);
		Position position2 = stores_num_and_position.get(storeNum2);
		Position position3 = stores_num_and_position.get(storeNum3);
		double x1 = position1.getX(), y1 = position1.getY();
		double x2 = position2.getX(), y2 = position2.getY();
		double x3 = position3.getX(), y3 = position3.getY();

		double alpha = angle1 / 180 * pi, beta = angle2 / 180 * pi;

		double a = Math.sqrt(square(x3 - x2) + square(y3 - y2));
		double b = Math.sqrt(square(x1 - x2) + square(y1 - y2));
		double phi = Math.acos(((x3 - x2) * (x1 - x2) + (y3 - y2) * (y1 - y2))
				/ (a * b));

		double sinBeta = Math.sin(beta);
		double sinBetaAndPhi = Math.sin(beta + phi);
		double cosBetaAndPhi = Math.cos(beta + phi);
		double cotAlpha = (Math.abs(Math.abs(angle1) - 90) < 1e-6) ? 0
				: 1 / Math.tan(alpha);

		double denominator = square(b * sinBetaAndPhi - a * sinBeta)
				+ square(b * cosBetaAndPhi + a * sinBeta * cotAlpha);
		double x0 = a * b * (sinBetaAndPhi * cotAlpha + cosBetaAndPhi)
				* (a * sinBeta * cotAlpha + b * cosBetaAndPhi) / denominator;
		double y0 = a * b * (sinBetaAndPhi * cotAlpha + cosBetaAndPhi)
				* (b * sinBetaAndPhi - a * sinBeta) / denominator;

		double answer[] = new double[2];
		answer[0] = x0;
		answer[1] = y0;

		return answer;
	}

	static private void getMapXml() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(map_xml));
			NodeList storeElements = document.getElementsByTagName("store");
			for (int i = 0; i < storeElements.getLength(); i++) {
				Element storeElement = (Element) storeElements.item(i);
				int storeNum = Integer.parseInt(storeElement
						.getAttribute("id"));
				String storeName = storeElement.getAttribute("name");

				Element locationElement = (Element) (storeElement
						.getElementsByTagName("location").item(0));
				Position position = new Position();
				position.setX(Double.parseDouble((locationElement
						.getAttribute("x"))));
				position.setY(Double.parseDouble((locationElement
						.getAttribute("y"))));

				stores_num_and_position.put(new Integer(storeNum), position);
				stores_num_and_name.put(new Integer(storeNum), storeName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static public String getStoreNameFromNum(int store_num) {
		if (stores_num_and_position.size() == 0) {
			getMapXml();
		}
		return stores_num_and_name.get(store_num);
	}

	static public Position getStorePositionFromNum(int store_num) {
		if (stores_num_and_position.size() == 0) {
			getMapXml();
		}
		return stores_num_and_position.get(store_num);
	}

	static private double square(double a) {
		return a * a;
	}

	static public ArrayList<String> getAllNumAndName() {
		if (stores_num_and_position.size() == 0) {
			getMapXml();
		}
		ArrayList<String> answers = new ArrayList<String>();
		for (int i = 1; i <= stores_num_and_position.size(); i++) {
			Integer currentInteger = new Integer(i);
			String currentName = stores_num_and_name.get(i);
			String numString = currentInteger.toString();
			answers.add(numString + currentName);
		}
		return answers;
	}

	static public double calOrientFromNorth(Position standingPosition, int currentStore) {
		Position currentStorePosition = getStorePositionFromNum(currentStore);
		double x1 = currentStorePosition.getX() - standingPosition.getX(), y1 = currentStorePosition.getY() - standingPosition.getY();
		double x2 = -1, y2 = 0;
		double cosCita = (x1 * x2 + y1 * y2) / (Math.sqrt(square(x1) + square(y1)) * Math.sqrt(square(x2) + square(y2)));

		if (cosCita > 1) cosCita = 1;
		if (cosCita < -1) cosCita = -1;

		double cos_angle = Math.acos(cosCita) / ConstantValue.pi * 180;
		if (x1 * y2 - x2 * y1 <= 0) {
			cos_angle = 360 - cos_angle;
		}
		return cos_angle;
	}
}
