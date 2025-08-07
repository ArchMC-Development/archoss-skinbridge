package mc.arch.skin.bridge.management

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer

/**
 * @author Subham
 * @since 8/6/25
 */
@AutoRegister
object ManageSkinConversionCommand : ScalaCommand()
{
    @CommandAlias("manageskinconversion")
    @CommandPermission("op")
    fun onManageSkinConversion(player: ScalaPlayer) = ManageSkinConversionConfigMenu().openMenu(player)
}
