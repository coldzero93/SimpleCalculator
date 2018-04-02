import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Stack;

public class SimpleCalc extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private boolean isInit = true; // 계산기의 초기화 상태를 나타내는 변수
	private boolean isDotEnabled = true; // 도트를 사용할 수 있는지 나타내는 변수
	private Font font = new Font("arian", Font.BOLD, 20); // 계산기 버튼들의 기본 폰트
	private String[] buttonVal = {"7", "8", "9", "*", // 계산기 버튼들의 값을 순서대로 표현
			  			  "4", "5", "6", "÷", 
			  			  "1", "2", "3", "+", 
			  			  "0", ".", "=", "-"  };
	private String operator = "+-*÷"; // 연산자에 해당하는 문자열
	private int numLeftPar;  // 입력된 왼쪽 괄호의 개수
	private int numRightPar; // 입력된 오른쪽 괄호의 개수
	
	private JTextField result = new JTextField("0", 22); // 입출력 결과값을 나타내는 텍스트 필드
	private JButton del = new JButton("←");				 // 삭제 버튼
	private JButton clear = new JButton("  C  ");		 // 초기화 버튼
	private JButton parenthesis = new JButton(" ( ) ");	 // 괄호 버튼
	private JButton[] button = new JButton[16];			 // 피연산자 및 연산자 버튼들
	private MyListener listener = new MyListener();		 // 버튼의 기능 수행을 위한 리스너
	
	// 계산기 창을 생성하는 메소드
	public void SwingFrame() {
		
		Panel1 p1 = new Panel1(); // 결과값 텍스트필드, 삭제 버튼, 초기화 버튼이 위치한 패널1
		Panel2 p2 = new Panel2(); // 피연사자 및 연사자 버튼들이 위치한 패널2
		
		add(p1);
		add(p2);
		
		setLayout(null);
		setSize(700, 500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("SimpleCalc");
		setVisible(true);
	}
	
	// 결과값 텍스트필드, 삭제 버튼, 초기화 버튼이 위치한 패널1
	private class Panel1 extends JPanel {

		private static final long serialVersionUID = 1L;

		public Panel1() {
			
			setBounds(40, 50, 600, 50);
			setBackground(Color.YELLOW);
			
			// 결과값 텍스트 필드를 수정이 불가능하도록 하며, 우측 정렬해 놓음
			result.setEnabled(false);
			result.setPreferredSize(new Dimension(80, 40));
			result.setHorizontalAlignment(JTextField.RIGHT);
			
			// 폰트 설정
			result.setFont(font);
			del.setFont(font);
			clear.setFont(font);
			parenthesis.setFont(font);
			
			// 리스너 부착
			result.addActionListener(listener);
			del.addActionListener(listener);
			clear.addActionListener(listener);
			parenthesis.addActionListener(listener);
			
			// 패널에 추가
			add(result);
			add(del);
			add(clear);
			add(parenthesis);
		}
	}
	
	// 피연사자 및 연사자 버튼들이 위치한 패널2
	private class Panel2 extends JPanel {

		private static final long serialVersionUID = 1L;

		public Panel2() {
			
			setBounds(40, 120, 600, 300);
			setBackground(Color.BLUE);
			setLayout(new GridLayout(4, 4));
			
			// 위에서 정의한 순서대로 버튼을 패널에 추가하며, 각각 폰트 설정 및 리스너 부착
			for(int i=0; i<16; i++) {
				button[i] = new JButton(buttonVal[i]);
				button[i].setFont(font);
				button[i].addActionListener(listener);
				add(button[i]);
			}
		}
	}
	
	// 해당 문자열이 연산자(+, -, *, /, .)인지 판별하는 메소드
	private boolean isOperator(CharSequence cs) { return operator.contains(cs); }
	
	// 중위 표기식을 후위 표기식으로 변환하는 메소드
	private String[] toPostfix(String infix) {
		
		Stack<String> stack = new Stack<String>(); // 스택 생성
		
		if(infix.charAt(0) == '-') infix = "0" + infix; // 맨 앞이 -인 경우, 맨 앞에 0을 추가로 입력하여 에러를 방지
		
		// 음수의 경우 앞에 0을 추가하여 -연산으로 인식되도록 수정
		char cur, next;
		for(int i=0; i<infix.length()-1; i++) {
			cur = infix.charAt(i);
			next = infix.charAt(i+1);
			if(next == '-' && !(cur >= '0' && cur <= '9')) {
				infix = infix.substring(0, i+1) + "0" + infix.substring(i+1, infix.length());
				i++;
			}
		}
		
		// 피연산자와 연산자를 구분하기 위한 전처리 (+와 *는 정규표현식에 중요한 기능을 수행하는 문자이므로 []로 처리하여 접근)
		infix = infix.replaceAll("[+]", " + ");
		infix = infix.replaceAll("-", " - ");
		infix = infix.replaceAll("[*]", " * ");
		infix = infix.replaceAll("÷", " ÷ ");
		infix = infix.replaceAll("\\(", "( ");
		infix = infix.replaceAll("\\)", " )");
		
		String[] tokens = infix.split(" "); // 피연산자와 연산자를 구분하여 문자열 배열에 저장
		String[] postfix = new String[tokens.length - (numLeftPar + numRightPar)]; // 후위 표기식을 담을 배열 선언
		int pos = 0;
		
		// 피연산자의 경우 무조건 출력, 연산자의 경우 자신보다 우선순위가 낮은 연산자가 스택에서 peek되는 경우에만 push하며 나머지 경우는 pop
		for(int i=0; i<tokens.length; i++) {
			if(tokens[i].equals("(")) stack.push("("); // 왼쪽 괄호가 나오면 스택에 push
			else if(tokens[i].equals(")")) { // 오른쪽 괄호가 나오면 왼쪽 괄호가 나올 때까지 스택을 pop하여 출력
				while(!stack.isEmpty() && !stack.peek().equals("("))
					postfix[pos++] = stack.pop();
				stack.pop(); // 스택에 있는 왼쪽 괄호 1개 제거
			} else if(tokens[i].equals("*") || tokens[i].equals("÷")) { // *나 /가 나오면 스택이 비어있지 않고, peek한 값이 *나 /라면 스택을 pop하여 출력을 반복, 이후 스택에 push
				while(!stack.isEmpty() && (stack.peek().equals("*") || stack.peek().equals("÷")))
					postfix[pos++] = stack.pop();
				stack.push(tokens[i]);
			} else if(tokens[i].equals("+") || tokens[i].equals("-")) { // +나 -가 나오면 스택이 비어있지 않고, peek한 값이 연산자라면 스택을 pop하여 출력을 반복, 이후 스택에 push
				while(!stack.isEmpty() && isOperator(stack.peek()))
					postfix[pos++] = stack.pop();
				stack.push(tokens[i]);
			} else postfix[pos++] = tokens[i]; // 피연산자는 무조건 출력
		}
		
		// 남아 있는 연산자나 괄호 pop하여 출력
		while(!stack.isEmpty()) postfix[pos++] = stack.pop();
				
		// 후위 표기식 반환
		return postfix;
	}
	
	// 후위 표기식을 계산하는 메소드
	private double calcPost(String[] postfix) {
	
		
		Stack<Double> stack = new Stack<Double>(); // 스택 생성
		double a, b; // 계산을 위한 변수 선언
		
		// 피연산자는 스택에 push하며, 연산자가 나올 경우 스택에서 두개의 값을 pop해와 계산 후 다시 push
		for(int i=0; i<postfix.length; i++) {
			if(isOperator(postfix[i])) {
				b = stack.pop(); // 스택은 후입선출이므로 역순으로 b, a를 할당해주는 것이 중요
				a = stack.pop();
				switch(postfix[i]) {
				case "+" : 
					stack.push(a + b);
					break;
				case "-" : 
					stack.push(a - b);
					break;
				case "*" : 
					stack.push(a * b);
					break;
				case "÷" : 
					stack.push(a / b);
					break;
				}
			} else {
				stack.push(Double.parseDouble(postfix[i]));
			}
		}
		
		return stack.pop(); // 최종 계산 결과를 스택에서 pop하여 반환
	}
	
	// 각 버튼의 기능 수행을 나타내는 리스너 생성
	private class MyListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			String clicked = ((JButton)e.getSource()).getText(); // 클릭된 값이 무엇인지 파악
			
			String origin = result.getText(); // 원본 텍스트 저장
			String lastStr = Character.toString(origin.charAt(origin.length()-1)); // 원본 텍스트의 마지막 문자열 저장
			
			// 1. 초기화(C) 버튼을 누른 경우
			if(clicked.equals("  C  ")) {
				
				result.setText("0"); // 입력 값을 0으로 변경 후
				isInit = true;		 // 초기화 상태로 선언
				isDotEnabled = true; // 도트 사용이 가능하도록 초기화
				numLeftPar = numRightPar = 0; // 괄호 개수 초기화
			
			// 2. 삭제(←) 버튼을 누른 경우
			} else if(clicked.equals("←")) {
				
				result.setText(origin.substring(0, origin.length()-1)); // 입력 값에서 마지막 문자열을 삭제
				
				if(result.getText().equals("")) { // 그 이후의 결과가 아무 값도 없다면
					result.setText("0"); 		  // 입력 값을 0으로 변경 후
					isInit = true;				  // 초기화 상태로 선언
				}
				
				// 괄호를 삭제한 경우, 입력된 괄호의 개수 차감
				if(lastStr.equals("(")) numLeftPar--;
				else if(lastStr.equals(")")) numRightPar--;
				
				if(lastStr.equals(".")) isDotEnabled = true; // 마지막으로 지운 문자열이 도트면, 도트 사용이 가능하도록 초기화
				else if(isOperator(lastStr)) {
					String nowStr = result.getText();
					String nowChar;
					isDotEnabled = true;
					for(int i=nowStr.length()-1; i>=0; i--) {
						nowChar = Character.toString(nowStr.charAt(i));
						if(isOperator(nowChar)) break;
						else if(nowChar.equals(".")) {
							isDotEnabled = false;
							break;
						}
					}
				}
			
			// 3. 괄호 버튼을 누른 경우
			} else if(clicked.equals(" ( ) ")) {

				if(isInit) { // (Left) 초기 상태인 경우
					result.setText("(");
					numLeftPar++;
					isInit = false;
				} else if(lastStr.equals("(")) { // (Left) 마지막 입력 값이 왼쪽 괄호인 경우
					result.setText(result.getText() + "(");
					numLeftPar++;
				} else {  // 나머지의 경우
//					if(isOperator(lastStr) || lastStr.equals("."))  // 마지막 입력 값이 연산자 도트인 경우
//						result.setText(origin.substring(0, origin.length()-1)); // 입력 값에서 마지막 문자열을 삭제
					if(numLeftPar == numRightPar) {  // *(Left) 괄호가 모두 닫힌 경우
						if(isOperator(lastStr)) result.setText(result.getText() + "(");
						else result.setText(result.getText() + "*(");
						numLeftPar++;
					} else { // (Right) 괄호가 하나라도 열린 경우
						result.setText(result.getText() + ")");
						numRightPar++;
					}
					
				}
				
			// 4. 계산(=) 버튼을 누른 경우
			} else if(clicked.equals("=")) {
				
				// 괄호가 덜 닫혔을 경우, 마저 닫아줌
				while(numLeftPar > numRightPar) {
					result.setText(result.getText() + ")");
					numRightPar++;
				}
				
				if(isOperator(lastStr)) result.setText(origin.substring(0, origin.length()-1)); // 마지막 문자열이 연산자면 삭제
				String[] postfix = toPostfix(result.getText()); // 중위 표기식을 후위 표기식으로 변환
				double calcResult = calcPost(postfix); // 후위 표기식을 계산
				
				// 소숫점을 나타낼 필요가 없다면 정수로, 있다면 실수로 출력
				if(calcResult == (int)calcResult) result.setText(Integer.toString((int)calcResult));
				else result.setText(Double.toString(calcResult));
				
				numLeftPar = numRightPar = 0; // 괄호 개수 초기화
				
			// 5. 도트(.) 버튼을 누른 경우
			} else if(clicked.equals(".")) {
				
				// 도트가 사용 가능한 경우
				if(isDotEnabled) {
					if(isOperator(lastStr) || lastStr.equals("(")) result.setText(result.getText() + "0."); // 마지막 입력 문자열이 연산자 또는 왼쪽 괄호면 "0." 입력
					else if(lastStr.equals(")")) result.setText(result.getText() + "*0."); // 마지막 입력 문자열이 오른쪽 괄호면 "*0." 입력
					else result.setText(result.getText() + "."); // 나머지의 경우 "." 입력
					isDotEnabled = false; // 도트 사용이 불가능하도록 설정
				}
				
			// 6. 연산자(*, /, +, -) 버튼을 누른 경우
			} else if(isOperator(clicked)) {
				
				// 초기화 상태 라면 초기화 해제
				if(isInit == true)
					isInit = false;
				
				// 마지막 문자열이 왼쪽 괄호면서
				if(lastStr.equals("(")) {
					// 입력한 연산자가 -면
					if(clicked.equals("-")) result.setText(result.getText() + clicked); // 입력 값에 연산자 추가
					// 입력한 연산자가 -가 아니라면 연산자 추가X
				
				// 그 이외의 경우
				} else {
					// 마지막 입력 값이 연산자라면, 마지막 입력된 연산자를 지운다
					if(isOperator(lastStr)) result.setText(origin.substring(0, origin.length()-1));
					result.setText(result.getText() + clicked); // 입력 값에 연산자 추가
					isDotEnabled = true; // 도트 사용이 가능하도록 초기화
				}
								
			// 7. 숫자(0~9) 버튼을 누른 경우
			} else {
				
				if(isInit == true) { 	// 초기화 상태라면
					result.setText(""); // 입력 값을 비운 뒤
					isInit = false; 	// 초기화 상태 해제
				}
				result.setText(result.getText() + clicked); // 입력 값에 숫자 추가
			}
		}
	}
	
	// 계산기 객체 생성 및 표시
	public static void main(String[] args) {
		SimpleCalc c = new SimpleCalc();
		c.SwingFrame();
	}

}