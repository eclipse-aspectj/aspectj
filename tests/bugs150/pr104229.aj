import java.lang.annotation.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

interface GoalSelectedNotice {
	  public void goalSelected(Object goal);
}

@Retention(RetentionPolicy.RUNTIME) 
@interface DefaultImplementation {}

aspect X {
	/**
	 * Watch for goalSelected(..) method being called when
	 * not within this aspect.
	 */
	pointcut goalSelectedPointcut(GoalSelectedNotice _this, Object goal): 
		call(void GoalSelectedNotice.goalSelected(Object))
	    && target(_this) && args(goal) && !cflow(adviceexecution());	
	
	declare warning : call(void GoalSelectedNotice.goalSelected(Object)) : "bingo";
	
	after(Object caller, Object o) returning : call(void GoalSelectedNotice.goalSelected(Object)) && args(o) && target(caller){
		System.out.println("call match " + caller.getClass());
	}
	
	after(GoalSelectedNotice _this, Object goal) returning:
	goalSelectedPointcut(_this, goal){
	  System.out.println("OK it worked!");
	}
	/**
	 * Empty body, can be overriden by classes implementing
	 * {@link GoalSelectedNotice}.
	 */
    @DefaultImplementation 
	public void GoalSelectedNotice.goalSelected(Object goal){
	}
}

public class pr104229 implements GoalSelectedNotice {
	
	interface CallMe { void doIt(); }
	
	public static void main(String[] args) {
		pr104229 pr = new pr104229();
		pr.callInner();
	}
	
	public void callInner() {
		
		CallMe callMe = new CallMe() {
			public void doIt() {
				pr104229.this.goalSelected("MyGoal");
			}
		};
		
		callMe.doIt();
		
	}
	
	
}

class ListPanel extends JPanel implements GoalSelectedNotice{

	private JComboBox jComboBox = null;
	private JList jList = null;
	/**
	 * This is the default constructor
	 */
	public ListPanel() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(300,200);
		this.add(getJComboBox(), java.awt.BorderLayout.NORTH);
		this.add(getJList(), java.awt.BorderLayout.CENTER);
	}
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox((ComboBoxModel)null);
		}
		return jComboBox;
	}
	/**
	 * This method initializes jList	
	 * 	
	 * @return javax.swing.JList	
	 */    
	private JList getJList() {
		if (jList == null) { 
			jList = new JList((ListModel)null);
			jList.addListSelectionListener(
					new  ListSelectionListener(){
						public void valueChanged(ListSelectionEvent e){
							if(!e.getValueIsAdjusting()){
								JList list = (JList)e.getSource();
								Object goal = list.getSelectedValue();
								System.out.println(goal);

// If I replace the line below with...
								ListPanel.this.goalSelected(goal); //the join point is not seen!;
// This was working before a switched to source level 5!				
				ListPanel.this.sendGoalSelectedNotice(goal); // this is workaround!

							}
						}
					}
					);
		}
		return jList;
	}

	// this is part of workaround
	protected void sendGoalSelectedNotice(Object goal){
                // join point is found by pointcut here!  This is ok!
		goalSelected(goal);
	}
}