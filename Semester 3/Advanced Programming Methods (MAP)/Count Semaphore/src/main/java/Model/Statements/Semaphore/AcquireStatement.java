package Model.Statements.Semaphore;

import Exceptions.AppException;
import Model.ADTs.Dictionary.ADT_I_Dictionary;
import Model.Statements.IStatement;
import Model.States.CountSemaphore.ISemaphoreTable;
import Model.States.ProgState;
import Model.States.SymTable.ISymTable;
import Model.Types.IType;
import Model.Types.IntegerType;
import Model.Values.IValue;
import Model.Values.IntegerValue;
import javafx.util.Pair;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AcquireStatement implements IStatement {
    private final String var;
    private static final Lock lock = new ReentrantLock();

    public AcquireStatement(String var) {
        this.var = var;
    }

    @Override
    public ProgState execute(ProgState state) throws AppException {
        lock.lock();
        ISymTable symTable = state.getSymTable();
        ISemaphoreTable semaphoreTable = state.getSemaphoreTable();
        if (symTable.isDefined(var)) {
            if (symTable.getValue(var).getType().equals(new IntegerType())){
                IntegerValue fi = (IntegerValue) symTable.getValue(var);
                int foundIndex = fi.getValue();
                if (semaphoreTable.getSemaphoreTable().containsKey(foundIndex)) {
                    Pair<Integer, List<Integer>> foundSemaphore = semaphoreTable.get(foundIndex);
                    int NL = foundSemaphore.getValue().size();
                    int N1 = foundSemaphore.getKey();
                    if (N1 > NL) {
                        if (!foundSemaphore.getValue().contains(state.getId())) {
                            foundSemaphore.getValue().add(state.getId());
                            semaphoreTable.update(foundIndex, new Pair<>(N1, foundSemaphore.getValue()));
                        }
                    } else {
                        state.getExecutionStack().push(this);
                    }
                } else {
                    throw new AppException("Index not a key in the semaphore table!");
                }
            } else {
                throw new AppException("Index must be of int type!");
            }
        } else {
            throw new AppException("Index not in symbol table!");
        }
        lock.unlock();
        return null;
    }

    @Override
    public ADT_I_Dictionary<String, IType> typecheck(ADT_I_Dictionary<String, IType> typeDictionary) throws AppException {
        if (typeDictionary.getValueForKey(var).equals(new IntegerType())) {
            return typeDictionary;
        } else {
            throw new AppException(String.format("%s is not int!", var));
        }
    }


    @Override
    public String toString() {
        return "Acquire(" + var + ")";
    }
}
