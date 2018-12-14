package ast.node.expression;

public enum UnaryOperator {
    not{
        @Override
        public String toString() {
          return "not";
        }
      }
    ,minus{
        @Override
        public String toString() {
          return "minus";
        }
      }
}
