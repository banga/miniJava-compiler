/**
 * Example illustrating components of mJAM package
 * @author prins
 * @version COMP 520 V2.2
 */
package mJAM;
import mJAM.Machine.Op;
import mJAM.Machine.Reg;
import mJAM.Machine.Prim;

// test class to construct and run an mJAM program
public class Test
{
	public static void main(String[] args){
		
		Machine.initCodeGen();
		System.out.println("Generating test program object code");
		
	/* class A {
	 *    int x;  
	 *    int p(){return x;} 
	 * }  
	 */
		int patchme_coA = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP,Reg.CB,0);        // jump to coA skipping code for methods in class A
		
		// code for p() in A
		int label_pA = Machine.nextInstrAddr();
/*pA*/	Machine.emit(Op.LOAD,Reg.OB,0);        // x at offset 0 in current instance of A or its subclass
		Machine.emit(Op.HALT,4,0,0);           // show state of stack after evaluating x
		Machine.emit(Op.RETURN,1,0,0);         // pop zero args, return one value
		
		// build class object for A at 0[SB]
		int label_coA = Machine.nextInstrAddr();
		Machine.patch(patchme_coA, label_coA);
/*coA*/ Machine.emit(Op.LOADL,-1);              // no superclass for A     
		Machine.emit(Op.LOADL,1);               // number of methods in A (1)
		Machine.emit(Op.LOADA,Reg.CB,label_pA); // code addr for method p_A
		
		
    /* class B extends A {
     *	  int y;  
     *    int p(){return x+11;} 
   	 * }
   	 */
		int patchme_coB = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP,Reg.CB,0); 		// jump to coB skipping code for methods in class B
		
		// code for p() in B
		int label_pB = Machine.nextInstrAddr();
/*pB*/  Machine.emit(Op.LOAD,Reg.OB,0);         // x at offset 0 in current instance
		Machine.emit(Op.LOADL,11);				// constant 11 on stack
		Machine.emit(Op.HALT,4,0,0);			// snapshot just before addition in pB
		Machine.emit(Prim.add);					// perform addition
		Machine.emit(Op.RETURN,1,0,0);    		// pop zero args, return 1 value
		
		// build class object for B at 3[SB]
		int label_coB = Machine.nextInstrAddr();
		Machine.patch(patchme_coB, label_coB);
/*coB*/ Machine.emit(Op.LOADA,Reg.SB,0);        // addr of superclass object (= coA at 0[SB])
		Machine.emit(Op.LOADL,1);               // number of methods in B
		Machine.emit(Op.LOADA,Reg.CB,label_pB); // code addr for method p_B

		
	/* class C { 
	 *    public static void main(String [] args) {
	 *      A a = new  A(); 
	 *      a.x = 44; 
	 *      System.out.println(a.p()); 
	 *      ...
	 */
		int patchme_coC = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP,Reg.CB,0);			// jump to coC skipping code for methods in class B
		
		// code for main() in C
		int label_mainC = Machine.nextInstrAddr();
/*mainC*/  Machine.emit(Op.HALT,4,0,0);
		Machine.emit(Op.PUSH,1);          		// reserve space for local var "a" at 3[LB]
		Machine.emit(Op.LOADA,Reg.SB,0);  		// class object for A
		Machine.emit(Op.LOADL,1);         		// size of A
		Machine.emit(Prim.newobj);				// create object in the heap        
		Machine.emit(Op.STORE,Reg.LB,3);  		// save its address in local var "a"
		Machine.emit(Op.LOAD,Reg.LB,3);			// address of object "a"
		Machine.emit(Op.LOADL,0);         		// field 0
		Machine.emit(Op.LOADL,44);        		// new value 44 for field
		Machine.emit(Op.HALT,4,0,0);			// snapshot
		Machine.emit(Prim.fieldupd);			// update a.x
		Machine.emit(Op.LOAD,Reg.LB,3);    		// addr of object "a"  (will be OB in p_A)
		Machine.emit(Op.CALL,Reg.CB,label_pA);  // call to known method p_A
		Machine.emit(Prim.putint);        		// print result
		
	/*      ...
	 *      A b = new  B(); 
	 *      b.x = 66;  
	 *      System.out.println(b.p()); 
	 *   } // end main
	 * } // end class C
	 */
		Machine.emit(Op.PUSH,1);          		// reserve space for local var "b" at 4[LB]
		Machine.emit(Op.LOADA,Reg.SB,3);  		// class object for B
		Machine.emit(Op.LOADL,2);         		// size of B
		Machine.emit(Prim.newobj);				// create object in the heap
		Machine.emit(Op.STORE,Reg.LB,4);  		// save its address in local var "b"
		Machine.emit(Op.LOAD,Reg.LB,4);   		// address of object "b"
		Machine.emit(Op.LOADL,0);         		// field 0
		Machine.emit(Op.LOADL,66);        		// new value 66 for field
		Machine.emit(Prim.fieldupd);			// update b.x
		Machine.emit(Op.HALT,4,0,0);			// snapshot
		Machine.emit(Op.LOAD,Reg.LB,4);   		// address of object "b" 
		Machine.emit(Op.CALLD,0);         		// dynamic call, method index 0 (method p)
												// should call p_B in this case since dynamic type is B
		Machine.emit(Prim.putint);		  		// print result
		Machine.emit(Op.RETURN,1,0,0);    		// pop one arg (instance addr), return no	 value
		
		// build class object for C at 6[SB]
		int label_coC = Machine.nextInstrAddr();
		Machine.patch(patchme_coC, label_coC);
/*coC*/ Machine.emit(Op.LOADL,-1);              // no superclass object     
		Machine.emit(Op.LOADL,0);               // number of methods
		
	/*
	 *  End of class declarations
	 *  generate call to main()
	 */
		Machine.emit(Op.LOADL,-1);				   // -1 instance addr for static call
		Machine.emit(Op.CALL,Reg.CB,label_mainC);  // call main()
		Machine.emit(Machine.Op.HALT,0,0,0);       // halt

		/* write code as an object file */
		String objectCodeFileName = "test.mJAM";
		ObjectFile objF = new ObjectFile(objectCodeFileName);
		System.out.print("Writing object code file " + objectCodeFileName + " ... ");
		if (objF.write()) {
			System.out.println("FAILED!");
			return;
		}
		else
			System.out.println("SUCCEEDED");	
		
		/* create asm file using disassembler */
		System.out.print("Writing assembly file ... ");
		Disassembler d = new Disassembler(objectCodeFileName);
		if (d.disassemble()) {
			System.out.println("FAILED!");
			return;
		}
		else
			System.out.println("SUCCEEDED");
		
		/* run code */
		System.out.println("Running code ... ");
		Interpreter.interpret(objectCodeFileName);

		System.out.println("*** mJAM execution completed");
	}
}
