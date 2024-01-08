package bctrace.trace;

public class AgentFactory implements bctrace.core.io.shiftleft.bctrace.AgentFactory {

  @Override
  public bctrace.core.io.shiftleft.bctrace.Agent createAgent() {
    return Agent.getInstance();
  }

  @Override
  public bctrace.core.io.shiftleft.bctrace.AgentHelp createHelp() {
    return new AgentHelp();
  }
}
