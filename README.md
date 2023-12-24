# UsbDebugSwitch
USB デバッグ の ON・ OFF 状態を常に端末の画面に表示し、素早くON・ OFF を切り替えられる Android アプリです。

使い方
----------

「画面オーバーレイ」を ON に設定するとオーバーレイアイコンが表示されます。オーバーレイアイコンは USB デバッグの有効・無効状態を示します。  
<img src="images/screenshot_app_on.png" width="200">  <img src="images/screenshot_app_off.png" width="200">

オーバーレイアイコンは本アプリがバックグラウンドに移動しても、本アプリが終了しても常に表示されます。  
<img src="images/screenshot_home_on.png" width="200">  <img src="images/screenshot_home_off.png" width="200">

オーバーレイアイコンをタップすると設定アプリが起動され、USBデバッグの有効/無効を切り替えることができます。  
<img src="images/screenshot_settings.png" width="200">

オーバーレイアイコンはドラッグ＆ドロップで任意の場所に移動させることができます。
<img src="images/screenshot_home_on_top.png" width="200">

開発メモ
----------

* 「他のアプリより上に重ねて表示」の権限を許可するためには「Settings.ACTION_MANAGE_OVERLAY_PERMISSION」のインテントアクションを実行する。  
    * Android11 未満では Uri で指定したアプリを対象とする設定画面が起動する。  
    * Android11 からは、「他のアプリより上に重ねて表示」できるアプリ一覧画面が起動し、ユーザーが対象のアプリを選択するように変更された。  

謝辞
----------

Icons made by <a href="https://www.flaticon.com/authors/flat-icons" title="Flat Icons">Flat Icons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a>

