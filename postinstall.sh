echo 'hacking...'
sed -i '' 's/\#import <RCTAnimation\/RCTValueAnimatedNode.h>/\#import \"RCTValueAnimatedNode.h\"/' ./node_modules/react-native/Libraries/NativeAnimation/RCTNativeAnimatedNodesManager.h
sed -i '' 's/\#import <fishhook\/fishhook.h>/\#import <React\/fishhook.h>/' ./node_modules/react-native/Libraries/WebSocket/RCTReconnectingWebSocket.m
echo 'hacked'
