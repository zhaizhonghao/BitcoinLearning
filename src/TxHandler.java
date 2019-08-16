import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private UTXOPool utxoPool;
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all inputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	double sumOfInputValue = 0;
    	double sumOfOutputValue = 0;
    	UTXOPool uniqueUtxos = new UTXOPool();
    	for (int i = 0; i < tx.numInputs(); i++) {	
			//ith input claimed by {@code tx} is in the current UTXO pool,
    		Transaction.Input input= tx.getInput(i);
    		UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
    		if (!utxoPool.contains(utxo)) {
				return false;
			}
    		// the signatures on ith input of {@code tx} are valid,
    		//the output corresponding to the ith input
    		Transaction.Output output= utxoPool.getTxOutput(utxo);
    		sumOfInputValue +=output.value;
    		if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature)) {
				return false;
			}
    		//no UTXO is claimed multiple times by {@code tx},
    		if (uniqueUtxos.contains(utxo)) {
				return false;
			}
    		uniqueUtxos.addUTXO(utxo, output);
		}
    	// all of {@code tx}s output values are non-negative
    	for (int i = 0; i < tx.numOutputs(); i++) {
    		Transaction.Output output = tx.getOutput(i);
    		
    		if (output.value < 0) {
				return false;
			}
    		sumOfOutputValue += output.value;
		}
    	//the sum of {@code tx}s input values is greater than or equal to the sum of its output
        //    values; and false otherwise.
    	if (sumOfInputValue < sumOfOutputValue) {
			return false;
		}
    	
    	return true;
    	
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	HashSet<Transaction> txVis = new HashSet<>();
    	for (int i = 0; i < possibleTxs.length; i++) {
			Transaction transaction = possibleTxs[i];
			if (txVis.contains(transaction)) {
				continue;
			}
			if (isValidTx(transaction)) {
				txVis.add(transaction);
				//remove the UTXOs corresponding to inputs of transaction
				for (int j = 0; j < transaction.numInputs(); j++) {
					Transaction.Input input = transaction.getInput(j);
					UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
					utxoPool.removeUTXO(utxo);
				}
				//add the UTXOs corresponding to outputs of transaction
				for (int j = 0; j < transaction.numOutputs(); j++) {
					Transaction.Output output = transaction.getOutput(j);
					UTXO utxo = new UTXO(transaction.getHash(),j);
					utxoPool.addUTXO(utxo, output);
				}
			}
		}
        Transaction[] ret = new Transaction[txVis.size()];
        int idx =0;
        for(Transaction tx : txVis)
            ret[idx++] = tx;
        return ret;
    	
    }

}
