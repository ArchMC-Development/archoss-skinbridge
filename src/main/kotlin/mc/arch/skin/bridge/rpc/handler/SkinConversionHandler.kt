package mc.arch.skin.bridge.rpc.handler

import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler
import mc.arch.skin.SkinConversionDataSync
import mc.arch.skin.rpc.NewSkinAvailableRequest
import mc.arch.skin.rpc.SkinConversionRPC
import mc.arch.skin.rpc.SkinConversionRequest

/**
 * @author Subham
 * @since 8/6/25
 */
class SkinConversionHandler : RPCHandler<SkinConversionRequest, Unit>
{
    override fun handle(
        request: SkinConversionRequest,
        context: RPCContext<Unit>
    )
    {
        if (!SkinConversionDataSync.acceptingRequests())
        {
            return
        }

        SkinConversionDataSync.submitTask(
            playerId = request.playerId,
            base64Data = request.eaglerSkinData
        ).thenAccept { result ->
            if (result.result == null)
            {
                return@thenAccept
            }

            SkinConversionRPC.availableRPC.callSync(
                NewSkinAvailableRequest(
                    playerId = request.playerId,
                    skinValue = result.result.textureValue!!,
                    skinSignature = result.result.textureSignature!!,
                )
            )
        }.whenComplete { _, throwable ->
            throwable?.printStackTrace()
        }
    }
}
