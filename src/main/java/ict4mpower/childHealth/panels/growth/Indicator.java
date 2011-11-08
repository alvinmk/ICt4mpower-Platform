package ict4mpower.childHealth.panels.growth;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import models.PatientInfo;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import storage.ApplicationSocketTemp;

public class Indicator implements Serializable, Comparable<Indicator> {
	private static final long serialVersionUID = -9155283621010002742L;
	
	private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private PatientInfo patientInfo;
	private float headCircumference;
	private float length;
	private float weight;
	private Date date;
	
	public Indicator(PatientInfo pi, float headCircumference, float length, float weight, Date date) {
		this.patientInfo = pi;
		this.headCircumference = headCircumference;
		this.length = length;
		this.weight = weight;
		this.date = date;
	}
	
	public String getAge(Component parent) {
		return getAgeValue(parent).getObject();
	}
	
	public StringResourceModel getAgeValue(Component parent) {
		Object[] arr = getAccurateAgeArray(true);
		if((Integer)arr[2] == 0) return new StringResourceModel((String)arr[0], parent, new Model<Integer>((Integer)arr[1]));
		return new StringResourceModel((String)arr[0]+"_extra_days", parent, new Model<AgeData>(
				new AgeData((Integer)arr[1], (Integer)arr[2])));
	}
	
	public Object[] getAccurateAgeArray() {
		return getAccurateAgeArray(false);
	}
	
	private Object[] getAccurateAgeArray(boolean returnExtraDays) {
		Calendar measured = Calendar.getInstance();
		measured.setTime(date);
		Calendar birth = Calendar.getInstance();
		birth.setTime(patientInfo.getBirthDate());
		int years = measured.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
		int months = measured.get(Calendar.MONTH) - birth.get(Calendar.MONTH);
		int days = measured.get(Calendar.DAY_OF_MONTH) - birth.get(Calendar.DAY_OF_MONTH);
		int extraDays = 0;
		
		// Check days
		if(days < 0) {
			months += years*12-1;
		}
		else {
			months += years*12;
		}
		Calendar b = (Calendar) birth.clone();
		b.add(Calendar.MONTH, months);
		extraDays = measured.get(Calendar.DAY_OF_MONTH) - b.get(Calendar.DAY_OF_MONTH);
		if(extraDays<0) {
			extraDays += b.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		if(months < 3) {
			// Less than 3 months old
			int weeks = measured.get(Calendar.WEEK_OF_YEAR) - birth.get(Calendar.WEEK_OF_YEAR);
			if(weeks<0) {
				weeks += birth.getActualMaximum(Calendar.WEEK_OF_YEAR);
			}
			b = (Calendar) birth.clone();
			b.add(Calendar.WEEK_OF_YEAR, weeks);
			extraDays = measured.get(Calendar.DAY_OF_WEEK) - b.get(Calendar.DAY_OF_WEEK);
			if(extraDays<0) {
				extraDays += 7;
				weeks -= 1;
			}
			if(returnExtraDays) return new Object[]{"weeks", weeks, extraDays};
			return new Object[]{"weeks", weeks+extraDays/7f};
		}
		else {
			// More than or equal to 3 months old, use months
			if(returnExtraDays) return new Object[]{"months", months, extraDays};
			return new Object[]{"months", months+extraDays/(float)measured.getActualMaximum(Calendar.DAY_OF_MONTH)};
		}
	}
	
	public float getHeadCircumference() {
		return headCircumference;
	}
	
	public float getLength() {
		return length;
	}
	
	public float getWeight() {
		return weight;
	}
	
	public String getDate() {
		return df.format(date);
	}
	
	public Date getDateValue() {
		return date;
	}
	
	public float getBmi() {
		return Math.round((this.weight/(Math.pow(this.length/100, 2)))*10)/10f;
	}
	
	public float getNormalHeadCircumference() {
		return getNormalValue(0);
	}
	
	public float getNormalLength() {
		return getNormalValue(7);
	}
	
	public float getNormalWeight() {
		return getNormalValue(14);
	}
	
	public float getNormalBmi() {
		return Math.round((getNormalWeight()/Math.pow(getNormalLength()/100, 2))*10)/10f;
	}
	
	private float getNormalValue(int index) {
		Object[] arr = getAccurateAgeArray();
		return getReferenceValues()[index][arr[0] == "weeks" ? (int)Math.floor((Float)arr[1])
				: (int)Math.floor((Float)arr[1]+10)];
	}
	
	private Float[][] getReferenceValues() {
		Float[][] references = null;
		ApplicationSocketTemp store = ApplicationSocketTemp.getApplicationSocketTemp();
		String gb = null;
		if(patientInfo.getSex() == PatientInfo.Sex.FEMALE) {
			gb = "girls";
		}
		else if(patientInfo.getSex() == PatientInfo.Sex.MALE) {
			gb = "boys";
		}
		Set<Object> refs = store.getData("ChildHealth", "GrowthReferenceValues:"+gb);
		for(Object o : refs) {
			// Only get first data entry - there should be only one
			references = (Float[][]) o;
		}
		return references;
	}

	public float getHeadCircumferenceValue() {
		return headCircumference;
	}

	public float getLengthValue() {
		return length;
	}

	public float getWeightValue() {
		return weight;
	}
	
	public PatientInfo getPatientInfo() {
		return patientInfo;
	}
	
	public void setPatientInfo(PatientInfo pi) {
		patientInfo = pi;
	}

	public void setHeadCircumference(float headCircumference) {
		this.headCircumference = headCircumference;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int compareTo(Indicator other) {
		return date.compareTo(other.date);
	}
}
