网络问题的处理是程序中的疑难区，难是难在各种网络环境的变化导致我们的客户端程序处理逻辑出现异常。我们写代码的时候不仅仅按照正常的网络环境来看待问题，还需要推演各种网络环境的变化带来的影响。不管是服务器出了问题，还是网络连接出现了问题。最后到用户手里的都是我们的客户端，容错等机制是必须的。

# 返回延迟问题

举个例子，上伪代码：

	public class AccountServerTask{

		private void updateAccount(Account account){
			// 异步修改服务器数
			isSuccess = updateAccountToServer();
			//服务器更新成功
			if(isSuccess){
                //拉取服务器最新数据
				Account newAccount = getAccountFromServer();
                //更新到本地
				saveAccountToLocal();
			}
		private void updateAccountToServer（）{
			 //修改服务器数据。
        }
		}
	}

上述是一个修改本地账号信息的一个流程，先将新的数据更新到服务器，然后把服务器的数据拉回到本地，并保存到本地，以达到一个完成修改账号信息的操作。然而，如果我们在外部多次调用这个接口的时候。就会出现最新的数据不是我最后修改的数据，直接表现在界面上显示了上一次操作的数据。这里排除了线程的影响，还存在一个原因。就是网络不稳定的时候，上一次异步修改服务器的返回在后，最后的异步修改服务器返回在前。导致你在修改界面修改的数据，返回到显示界面又不对应。

# 返回数据错误问题

	public void getAccountFromServer(){
		String responseStr = httpResponse();
        Account account = JsonUtil.fromJson(responseStr，Account.class);
	}

这个问题估计大家很多时候都有处理过，当服务器返回的数据格式不对，甚至直接返回了404的Html。这个的Json解析出现异常，直接导致程序奔溃了。



# 数据同步问题


# 网络判断问题

+ WIFI判断：再进行下载的时候，我们通常判断是WiFi还是数据网络的时候。如果是WiFi则直接下载，但是如果我们在进行WiFi判断时仅判断WiFi开关是否打开，而没有判断WiFi是否连接。如果用户这个时候同时打开WiFi和数据网络开关，这时会用数据进行下载，在用户不清楚的情况下用流量下载。会遭投诉。

+ 是否有网判断：通过Android的API无法完全真正的判断是否为有效网络。比如连接无效的WiFi，插上的是欠费的SIM卡。这个时候，程序以为是有网，实际网络无效。如果存在重试的机制一定要注意是否进行合理的限制。保证在这种极端的情况下，能够很好的运行。