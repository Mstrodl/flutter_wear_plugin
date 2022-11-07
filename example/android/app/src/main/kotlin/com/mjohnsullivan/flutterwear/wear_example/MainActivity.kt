package com.mjohnsullivan.flutterwear.wear_example

import com.mjohnsullivan.flutterwear.wear.WearProvider
import io.flutter.embedding.android.FlutterFragmentActivity

class MainActivity: FlutterFragmentActivity(), WearProvider by WearProvider.Delegate.instance {
}
