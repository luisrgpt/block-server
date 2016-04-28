package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import pt.ulisboa.tecnico.sec.filesystem.common.AckFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedMac;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedSignature;
import pt.ulisboa.tecnico.sec.filesystem.common.HashBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.KeyBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.ProcessId;
import pt.ulisboa.tecnico.sec.filesystem.common.ReadFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.ValueFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.WriteFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;
import pt.ulisboa.tecnico.sec.filesystem.logging.FileSystemLogger;
import pt.ulisboa.tecnico.sec.filesystem.logging.FileSystemLoggerException;

final class RetransmitForeverAlgorithm
implements StubbornPointToPointLinks,
FairLossPointToPointLinksListener {
	private StubbornPointToPointLinksListener _stubbornPointToPointLinksListener;
	private FairLossPointToPointLinks _fairLossPointToPointLinks;

	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<WriteFlag, Integer, EncodedMac>, ImmutablePair<KeyBlock, EncodedSignature>>>> _sentWriteKeyBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<WriteFlag, Integer, EncodedMac>, HashBlock>>> _sentWriteHashBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<WriteFlag, Integer, EncodedMac>, ImmutablePair<EncodedPublicKey, EncodedSignature>>>> _sentWritePublicKey;
	private Set<ImmutablePair<ProcessId, ImmutableTriple<AckFlag, Integer, EncodedMac>>> _sentAck;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<ReadFlag, Integer, EncodedMac>, BlockId>>> _sentReadFileBlock;
	private Set<ImmutablePair<ProcessId, ImmutableTriple<ReadFlag, Integer, EncodedMac>>> _sentReadPublicKeys;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<ValueFlag, Integer, EncodedMac>, ImmutableTriple<Integer, KeyBlock, EncodedSignature>>>> _sentValueKeyBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<ValueFlag, Integer, EncodedMac>, HashBlock>>> _sentValueHashBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<ValueFlag, Integer, EncodedMac>, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>>>>> _sentValuePublicKeys;

	RetransmitForeverAlgorithm(ProcessId processes[], ProcessId process, StubbornPointToPointLinksListener stubbornPointToPointLinksListener)
			throws FileSystemException {
		try {
			_stubbornPointToPointLinksListener = stubbornPointToPointLinksListener;
			_fairLossPointToPointLinks = new RmiNode(processes, process, this);

			_sentWriteKeyBlock = new HashSet<>();
			_sentWriteHashBlock = new HashSet<>();
			_sentWritePublicKey = new HashSet<>();
			_sentAck = new HashSet<>();
			_sentReadFileBlock = new HashSet<>();
			_sentReadPublicKeys = new HashSet<>();
			_sentValueKeyBlock = new HashSet<>();
			_sentValueHashBlock = new HashSet<>();
			_sentValuePublicKeys = new HashSet<>();

			startTimer();
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	private void startTimer() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException exception) {
					try {
						FileSystemLogger.logWarning(exception.getMessage());
					} catch (FileSystemLoggerException fileSystemLoggerException) {
						System.out.println(fileSystemLoggerException.getMessage());
					}
				} finally {
					onTimeout();
				}
			}
		}).start();
	}

	private void onTimeout() {
		for(ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<WriteFlag, Integer, EncodedMac>, ImmutablePair<KeyBlock, EncodedSignature>>> message : _sentWriteKeyBlock) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft().getLeft(), message.getRight().getLeft().getMiddle(), message.getRight().getRight().getLeft(), message.getRight().getRight().getRight(), message.getRight().getLeft().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}
		for(ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<WriteFlag, Integer, EncodedMac>, HashBlock>> message : _sentWriteHashBlock) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft().getLeft(), message.getRight().getLeft().getMiddle(), message.getRight().getRight(), message.getRight().getLeft().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}
		for(ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<WriteFlag, Integer, EncodedMac>, ImmutablePair<EncodedPublicKey, EncodedSignature>>> message : _sentWritePublicKey) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft().getLeft(), message.getRight().getLeft().getMiddle(), message.getRight().getRight().getLeft(), message.getRight().getRight().getRight(), message.getRight().getLeft().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}
		for(ImmutablePair<ProcessId, ImmutableTriple<AckFlag, Integer, EncodedMac>> message : _sentAck) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft(), message.getRight().getMiddle(), message.getRight().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}
		for(ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<ReadFlag, Integer, EncodedMac>, BlockId>> message : _sentReadFileBlock) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft().getLeft(), message.getRight().getLeft().getMiddle(), message.getRight().getRight(), message.getRight().getLeft().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}
		for(ImmutablePair<ProcessId, ImmutableTriple<ReadFlag, Integer, EncodedMac>> message : _sentReadPublicKeys) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft(), message.getRight().getMiddle(), message.getRight().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}
		for(ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<ValueFlag, Integer, EncodedMac>, ImmutableTriple<Integer, KeyBlock, EncodedSignature>>> message : _sentValueKeyBlock) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft().getLeft(), message.getRight().getLeft().getMiddle(), message.getRight().getRight().getLeft(), message.getRight().getRight().getMiddle(), message.getRight().getRight().getRight(), message.getRight().getLeft().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}
		for(ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<ValueFlag, Integer, EncodedMac>, HashBlock>> message : _sentValueHashBlock) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft().getLeft(), message.getRight().getLeft().getMiddle(), message.getRight().getRight(), message.getRight().getLeft().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}
		for(ImmutablePair<ProcessId, ImmutablePair<ImmutableTriple<ValueFlag, Integer, EncodedMac>, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>>>> message : _sentValuePublicKeys) {
			try {
				_fairLossPointToPointLinks.onSend(message.getLeft(), message.getRight().getLeft().getLeft(), message.getRight().getLeft().getMiddle(), message.getRight().getRight(), message.getRight().getLeft().getRight());
			} catch (RemoteException | FileSystemException exception) {
				try {
					FileSystemLogger.logWarning(exception.getMessage());
				} catch (FileSystemLoggerException fileSystemLoggerException) {
					System.out.println(fileSystemLoggerException.getMessage());
				}
			}
		}

		startTimer();
	}

	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, keyBlock, encodedSignature, encodedMac);
			_sentWriteKeyBlock.add(new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutableTriple<>(writeFlag, writeTimeStamp, encodedMac), new ImmutablePair<>(keyBlock, encodedSignature))));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, HashBlock hashBlock, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, hashBlock, encodedMac);
			_sentWriteHashBlock.add(new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutableTriple<>(writeFlag, writeTimeStamp, encodedMac), hashBlock)));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, encodedPublicKey, encodedSignature, encodedMac);
			_sentWritePublicKey.add(new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutableTriple<>(writeFlag, writeTimeStamp, encodedMac), new ImmutablePair<>(encodedPublicKey, encodedSignature))));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, ackFlag, timeStamp, encodedMac);
			_sentAck.add(new ImmutablePair<>(processId, new ImmutableTriple<>(ackFlag, timeStamp, encodedMac)));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, readFlag, readId, blockId, encodedMac);
			_sentReadFileBlock.add(new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutableTriple<>(readFlag, readId, encodedMac), blockId)));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, readFlag, readId, encodedMac);
			_sentReadPublicKeys.add(new ImmutablePair<>(processId, new ImmutableTriple<>(readFlag, readId, encodedMac)));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, valueFlag, readId, timeStamp, keyBlock, encodedSignature, encodedMac);
			_sentValueKeyBlock.add(new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutableTriple<>(valueFlag, readId, encodedMac), new ImmutableTriple<>(timeStamp, keyBlock, encodedSignature))));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock hashBlock, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, valueFlag, readId, hashBlock, encodedMac);
			_sentValueHashBlock.add(new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutableTriple<>(valueFlag, readId, encodedMac), hashBlock)));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException {
		try {
			_fairLossPointToPointLinks.onSend(processId, valueFlag, readId, encodedPublicKeys, encodedMac);
			_sentValuePublicKeys.add(new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutableTriple<>(valueFlag, readId, encodedMac), encodedPublicKeys)));
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, keyBlock, encodedSignature, encodedMac);
	}

	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, hashBlock, encodedMac);
	}

	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, encodedPublicKey, encodedSignature, encodedMac);
	}

	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, readFlag, readId, blockId, encodedMac);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, readFlag, readId, encodedMac);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, keyBlock, encodedSignature, encodedMac);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock hashBlock, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, valueFlag, readId, hashBlock, encodedMac);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException {
		_stubbornPointToPointLinksListener.onDeliver(processId, valueFlag, readId, encodedPublicKeys, encodedMac);
	}
}
