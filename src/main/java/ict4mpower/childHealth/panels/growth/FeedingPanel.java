/*
 *  This file is part of the ICT4MPOWER platform.
 *
 *  The ICT4MPOWER platform is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The ICT4MPOWER platform is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with the ICT4MPOWER platform.  If not, see <http://www.gnu.org/licenses/>.
 */
package ict4mpower.childHealth.panels.growth;

import ict4mpower.AppSession;
import ict4mpower.childHealth.Callback;
import ict4mpower.childHealth.ExtendableDropDownChoice;
import ict4mpower.childHealth.SavingForm;
import ict4mpower.childHealth.data.GrowthData;
import ict4mpower.childHealth.panels.DivisionPanel;
import ict4mpower.childHealth.panels.TextDialog;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import storage.MedicalRecordSocket;
/**
 * Panel for feeding information
 * @author Joakim Lindskog
 *
 */
public class FeedingPanel extends DivisionPanel {
	private static final long serialVersionUID = 4179450386998797319L;
	
	private List<String> feeding = new ArrayList<String>(
			Arrays.asList(new String[] {
			"breast",
			"replacement",
			"complementary",
			"mixed",
			"other"}));

	/**
	 * Constructor
	 * @param id component id
	 */
	public FeedingPanel(String id) {
		super(id, "title");
		
		FeedingForm form = new FeedingForm("form");
		add(form);
		
		setForm(form, this);
	}
	
	/**
	 * Form for the feeding panel
	 * @author Joakim Lindskog
	 *
	 */
	private class FeedingForm extends SavingForm {
		private static final long serialVersionUID = 7663790124524711200L;

		/**
		 * Constructor
		 * @param id component id
		 */
		public FeedingForm(String id) {
			super(id);
			
			final GrowthData data = GrowthData.instance();
			if(data.getFeeding() == null) {
				Date max = null;
				try {
					max = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/1800");
				} catch (ParseException e) {
					e.printStackTrace();
				}
				GrowthData gd = null;
				MedicalRecordSocket socket = new MedicalRecordSocket();
				Set<Object> set = socket.getEntriesForPatientId(((AppSession)getSession()).getPatientInfo().getClientId(), GrowthData.class.getSimpleName(), "ChildHealth");
				for(Object o : set) {
					if(o instanceof GrowthData) {
						gd = (GrowthData) o;
						if(gd.getFeeding() != null && gd.getDate().after(max)) {
							data.setFeeding(gd.getFeeding());
							max = gd.getDate();
						}
					}
				}
			}
			
			// Dialog
			final TextDialog dialog = new TextDialog("dialog");
			
			add(dialog, false);
			
			final ExtendableDropDownChoice<String> feedingChoice =
					new ExtendableDropDownChoice<String>("feeding",
					new PropertyModel<String>(data, "feeding"), feeding);
			add(feedingChoice);
			feedingChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					if(feedingChoice.getConvertedInput() == null) return;
					if(feedingChoice.getConvertedInput().equals("other")) {
						// Show input dialog
						dialog.setTitle(new StringResourceModel("other_feeding", FeedingPanel.this, null));
						dialog.addOnSubmit(new Callback() {
							private static final long serialVersionUID = 1L;

							public boolean call(AjaxRequestTarget target) {
								final String text = dialog.getText();
								if(text == null || text.isEmpty()) {
									dialog.error(target, "feeding_name_error");
									return false;
								}
								feeding.add(feeding.size()-1, text);
								data.setFeeding(text);
								feedingChoice.modelChanged();
								target.add(feedingChoice);
								dialog.close(target);
								return true;
							}
						});
						dialog.setLabel(getString("feeding_name"));
						dialog.setCloseButtonCallback(new CloseButtonCallback() {
							private static final long serialVersionUID = 1L;

							public boolean onCloseButtonClicked(AjaxRequestTarget target) {
								data.setFeeding(null);
								feedingChoice.modelChanged();
								target.add(feedingChoice);
								return true;
							}
						});
						dialog.show(target);
					}
					target.add(feedingChoice);
				}
			});
		}
		
		@Override
		protected void onSubmit() {
			GrowthData.instance().setDate(new Date());
			
			super.onSubmit();
		}
	}
}
