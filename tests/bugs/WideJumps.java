// for need to fill in bug #


public class WideJumps {
    public static void main(String[] args) {
    	new WideJumps().m(true);
    }
    
    public void m(boolean b) {
    	if (b) {
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
			m(1,2,3,4,5,6,7,8,9,0);
    	}	
    }
    
    private void m(int i0, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
    }
}

aspect A  {
    Object around(): call(void m(..)) {
    	System.out.println("around: " + thisJoinPoint);
    	return proceed();
    }
    
	after(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i0):
		call(void m(..)) && args(i1, i2, i3, i4, i5, i6, i7, i8, i9, i0) && if(i1<i2&&i3<i4&&i5<i6&&i7<i8&&i9<i0)
	{
		System.out.println("after: " + thisJoinPoint);
	}
	after(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i0):
		call(void m(..)) && args(i1, i2, i3, i4, i5, i6, i7, i8, i9, i0) && if(i1<i2&&i3<i4&&i5<i6&&i7<i8&&i9<i0)
	{
		System.out.println("after: " + thisJoinPoint);
	}
	after(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i0):
		call(void m(..)) && args(i1, i2, i3, i4, i5, i6, i7, i8, i9, i0) && if(i1<i2&&i3<i4&&i5<i6&&i7<i8&&i9<i0)
	{
		System.out.println("after: " + thisJoinPoint);
	}
}
