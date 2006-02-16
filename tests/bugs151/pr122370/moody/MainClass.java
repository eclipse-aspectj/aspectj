package moody;

public class MainClass {

	public static void main(String[] args) {
		AnnotationMoodImplementor ami0 = new AnnotationMoodImplementor();
		AnnotationMoodImplementor ami1 = new AnnotationMoodImplementor();

		System.out.println("ami0's mood is " + ((AnnotationMoodIndicator.Moody) ami0).getMood());
		((AnnotationMoodIndicator.Moody) ami1).setMood(Mood.JOLLY);
		System.out.println("ami1's mood is now " + ((AnnotationMoodIndicator.Moody) ami1).getMood());
		System.out.println("ami0's mood is still " + ((AnnotationMoodIndicator.Moody) ami0).getMood());
	}
	

	
}
