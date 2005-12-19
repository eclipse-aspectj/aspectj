package moodytest;

import moodytest.AnnotationMoodyImplementor;
import moodytest.Mood;
import junit.framework.TestCase;

public class AnnotationMoodTester extends TestCase {
	   AnnotationMoodyImplementor ami0 = null;
	   AnnotationMoodyImplementor ami1 = null;

	   public AnnotationMoodTester(String name) { super(name); }

	   protected void setUp() throws Exception {
	      ami0 = new AnnotationMoodyImplementor();
	      ami1 = new AnnotationMoodyImplementor();
	   }

	   public void testHappyDefault() {
	      assertEquals("ami0 should be happy!", Mood.HAPPY, ami0.getMood());
	   }

	   public void testOneConfused() {
	      ami0.setMood(Mood.CONFUSED);
	      assertEquals("ami0 should now be confused", Mood.CONFUSED,
	 ami0.getMood());
	      assertEquals("ami1 should still be happy", Mood.HAPPY,
	 ami1.getMood());
	   }
}
