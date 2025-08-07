package mc.arch.skin.bridge.rpc

import mc.arch.commons.communications.rpc.CommunicationGateway
import mc.arch.commons.communications.rpc.createOneWayRemoteService

/**
 * @author Subham
 * @since 8/6/25
 */
object SkinConversionRPC
{
    val gateway = CommunicationGateway("skinconversion")
    val convertSkinRPC = gateway.createOneWayRemoteService<SkinConversionRequest>("convert")
    val availableRPC = gateway.createOneWayRemoteService<NewSkinAvailableRequest>("available")
}
